package io.evm.history.service;

import io.evm.history.config.EvmHistoryConfig;
import io.evm.history.service.model.TransactionReceiptContractWrapper;
import io.evm.history.util.block.IterableBlocks;
import io.evm.history.util.retry.RetryUtil;
import io.evm.history.util.retry.ThrottlingUtil;
import io.smallrye.mutiny.Multi;
import io.smallrye.mutiny.Uni;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import lombok.extern.jbosslog.JBossLog;
import org.web3j.protocol.Web3j;
import org.web3j.protocol.core.BatchRequest;
import org.web3j.protocol.core.DefaultBlockParameter;
import org.web3j.protocol.core.DefaultBlockParameterName;
import org.web3j.protocol.core.Request;
import org.web3j.protocol.core.methods.response.*;

import java.math.BigInteger;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@JBossLog
@ApplicationScoped
public class BlockAndReceiptsService {

    @Inject
    EvmHistoryConfig config;
    @Inject
    Web3j eth;

    @SuppressWarnings("ReactiveStreamsUnusedPublisher")
    public Multi<TransactionReceiptContractWrapper> data(BigInteger lastExistedBlock) {
        if (lastExistedBlock != null && (lastExistedBlock.compareTo(config.blockFrom()) < 0 || lastExistedBlock.compareTo(config.blockTo()) > 0)) {
            return Multi.createFrom()
                    .failure(new IllegalArgumentException("lastExistedBlock:%s should be in range of [blockFrom:%s - blockTo:%s]".formatted(lastExistedBlock, config.blockFrom(), config.blockTo())));
        } else {
            // iterate from / to
            Multi<List<TransactionReceiptContractWrapper>> retval = Multi.createFrom()
                    .iterable(new IterableBlocks(lastExistedBlock != null ? lastExistedBlock : config.blockFrom(), config.blockTo()))
                    // ------------ blocks ------------
                    // group in batches
                    .group()
                    .intoLists()
                    .of(config.blockBatch())
                    .onItem()
                    // concatenate without prefetch for not cache data that we are not using right now and not overload the source.
                    .transformToUniAndConcatenate(this::getBlocks);
            retval = ThrottlingUtil.throttling(config.throttling().orElse(null), retval);
            // ------------ receipts ------------
            retval = retval.concatMap(e -> Multi.createFrom().iterable(e))
                    .group()
                    .intoLists()
                    .of(config.receiptBatch())
                    .onItem()
                    .transformToUniAndConcatenate(this::getReceipts);
            retval = ThrottlingUtil.throttling(config.throttling().orElse(null), retval);
            // ------------ contracts ------------
            retval = retval.concatMap(e -> Multi.createFrom().iterable(e)).group()
                    .intoLists()
                    .of(config.contractBatch())
                    .onItem()
                    .transformToUniAndConcatenate(this::getContract);
            retval = ThrottlingUtil.throttling(config.throttling().orElse(null), retval);
            return retval.concatMap(e -> Multi.createFrom().iterable(e));
        }
    }

    private Uni<List<TransactionReceiptContractWrapper>> getBlocks(List<BigInteger> blocks) {
        String signature = "%s[%s-%s]".formatted(blocks.size(), blocks.getFirst(), blocks.getLast());
        log.infof("Blocks:%s", signature);
        // send batch request
        BatchRequest batch = eth.newBatch();
        for (BigInteger i : blocks) {
            batch.add(eth.ethGetBlockByNumber(DefaultBlockParameter.valueOf(i), true));
        }
        return RetryUtil.retryBatch(log, "blocks(%s)".formatted(signature), config, batch.sendAsync())
                // block time logging
                .invoke(e -> {
                    if (e.getResponses() != null && !e.getResponses().isEmpty()) {
                        log.debugf("Got Blocks:%s from:%s", e.getResponses()
                                .size(), Instant.ofEpochSecond(((EthBlock.Block) e.getResponses().getFirst()
                                .getResult()).getTimestamp().longValue()));
                    }
                })
                .map(resp -> resp.getResponses()
                        .stream()
                        .map(e -> (EthBlock.Block) e.getResult())
                        .filter(e -> e != null && e.getTransactions() != null)
                        .flatMap(block -> block.getTransactions().stream()
                                .map(e -> (Transaction) e.get())
                                .map(e -> new TransactionReceiptContractWrapper(block, e))).toList());
    }

    public Uni<List<TransactionReceiptContractWrapper>> getReceipts(List<TransactionReceiptContractWrapper> txs) {
        String signature = "%s[%s-%s]".formatted(txs.size(), txs.getFirst().getTx().getHash(), txs.getLast()
                .getTx()
                .getHash());
        log.infof("Receipts:%s", signature);
        // send batch request
        BatchRequest batch = eth.newBatch();
        for (int i = 0; i < txs.size(); i++) {
            Request<?, EthGetTransactionReceipt> req = eth.ethGetTransactionReceipt(txs.get(i).getTx().getHash());
            req.setId(i); // for mapping back
            batch.add(req);
        }
        return RetryUtil.retryBatch(log, "receipts(%s)".formatted(signature), config, batch.sendAsync())
                .invoke(resp -> resp.getResponses()
                        .forEach(e -> {
                            TransactionReceipt receipt = (TransactionReceipt) e.getResult();
                            if (receipt != null) {
                                TransactionReceiptContractWrapper wrapper = txs.get((int) e.getId());
                                if (wrapper != null) {
                                    wrapper.setReceipt(receipt);
                                } else {
                                    log.errorf("Cat find Tx.hash for reciept:%s", receipt.getTransactionHash());
                                }
                            }
                        })
                )
                .map(e -> txs);
    }

    public Uni<List<TransactionReceiptContractWrapper>> getContract(List<TransactionReceiptContractWrapper> txs) {
        String signature = "%s[%s-%s]".formatted(txs.size(), txs.getFirst().getTx().getHash(), txs.getLast()
                .getTx()
                .getHash());
        // group for mapping
        log.infof("Contracts:%s", signature);
        // used for batch response backward mapping. Contract code added for first transaction in batch, where it is found
        List<List<TransactionReceiptContractWrapper>> listOfTxTo = new ArrayList<>();
        // send batch request
        BatchRequest batch = eth.newBatch();
        //TODO add caching check
        txs.stream()
                .filter(e -> e.getReceipt().getContractAddress() != null)
                .collect(Collectors.groupingBy(e -> e.getReceipt().getContractAddress()))
                .forEach((to, list) -> {
                    Request<?, EthGetCode> req = eth.ethGetCode(to, DefaultBlockParameterName.LATEST);
                    req.setId(listOfTxTo.size());
                    batch.add(req);
                    listOfTxTo.add(list);
                });
        return RetryUtil.retryBatch(log, "contracts(%s)".formatted(signature), config, batch.sendAsync())
                .invoke(resp -> resp.getResponses()
                        .forEach(r -> {
                            String code = (String) r.getResult();
                            if (code != null) {
                                List<TransactionReceiptContractWrapper> wrapper = listOfTxTo.get((int) r.getId());
                                int codeBytesLength = code.getBytes().length;
                                if (wrapper != null) {
                                    wrapper.getFirst().setCreateContract(true);
                                    wrapper.forEach(e -> e.setCodeBytesLength(codeBytesLength));
                                }
                            }
                        })
                )
                .map(e -> txs);
    }
}

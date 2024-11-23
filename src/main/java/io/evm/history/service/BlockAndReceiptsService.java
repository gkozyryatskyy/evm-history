package io.evm.history.service;

import io.evm.history.config.EvmHistoryConfig;
import io.evm.history.service.model.BlockAndReceiptsWrapper;
import io.evm.history.service.model.BlockWrapper;
import io.evm.history.util.block.IterableBlocks;
import io.evm.history.util.retry.RetryUtil;
import io.smallrye.mutiny.Multi;
import io.smallrye.mutiny.Uni;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import lombok.extern.jbosslog.JBossLog;
import org.web3j.protocol.Web3j;
import org.web3j.protocol.core.BatchRequest;
import org.web3j.protocol.core.DefaultBlockParameter;
import org.web3j.protocol.core.methods.response.EthBlock;
import org.web3j.protocol.core.methods.response.Transaction;
import org.web3j.protocol.core.methods.response.TransactionReceipt;

import java.math.BigInteger;
import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

@JBossLog
@ApplicationScoped
public class BlockAndReceiptsService {

    @Inject
    EvmHistoryConfig config;
    @Inject
    Web3j eth;

    public Multi<BlockAndReceiptsWrapper> data(BigInteger lastExistedBlock) {
        if (lastExistedBlock != null && (lastExistedBlock.compareTo(config.blockFrom()) < 0 || lastExistedBlock.compareTo(config.blockTo()) > 0)) {
            return Multi.createFrom()
                    .failure(new IllegalArgumentException("lastExistedBlock:%s should be in range of [blockFrom:%s - blockTo:%s]".formatted(lastExistedBlock, config.blockFrom(), config.blockTo())));
        } else {
            // iterate from / to
            Multi<List<BlockAndReceiptsWrapper>> retval = Multi.createFrom()
                    .iterable(new IterableBlocks(lastExistedBlock != null ? lastExistedBlock : config.blockFrom(), config.blockTo()))
                    // ------------ blocks ------------
                    // group in batches
                    .group()
                    .intoLists()
                    .of(config.blockBatch())
                    .onItem()
                    // concatenate without prefetch for not cache data that we are not using right now and not overload the source.
                    .transformToUniAndConcatenate(this::getBlocks)
                    .concatMap(e -> Multi.createFrom().iterable(e))
            if (config.throttling().isPresent()) {
                // throttle each block batch for not get relate limit
                //noinspection OptionalGetWithoutIsPresent
                retval = retval.call(() -> Uni.createFrom()
                        .nullItem()
                        .onItem()
                        .delayIt()
                        .by(Duration.ofMillis(config.throttling().get())));
            }
            //noinspection ReactiveStreamsUnusedPublisher
            return retval.concatMap(e -> Multi.createFrom().iterable(e));
        }
    }

    private Uni<List<BlockAndReceiptsWrapper>> getBlocks(List<BigInteger> group) {
        log.infof("Blocks:%s[%s-%s]", group.size(), group.getFirst(), group.getLast());
        // send batch request
        BatchRequest batch = eth.newBatch();
        for (BigInteger i : group) {
            batch.add(eth.ethGetBlockByNumber(DefaultBlockParameter.valueOf(i), true));
        }
        return RetryUtil.retryBatch(log, "blocks(size:%s %s-%s)".formatted(group.size(), group.getFirst(), group.getLast()), config, batch.sendAsync())
                .invoke(e -> log.debugf("Got Blocks:%s from:%s", e.size(), Instant.ofEpochMilli(e.getFirst()
                        .timestamp())))

                .map(resp -> resp.getResponses()
                        .stream()
                        .map(e -> new BlockAndReceiptsWrapper((EthBlock.Block) e.getResult()))
                        .toList())
                .chain(this::receipts)
                .chain(this::contracts);
    }

    public Uni<List<BlockAndReceiptsWrapper>> receipts(List<BlockAndReceiptsWrapper> blocks) {
        return Multi.createFrom()
                .items(blocks.stream()
                        .map(BlockWrapper::getBlock)
                        // time to time testnet return null blocks. Filtering it out
                        .filter(e -> e != null && e.getTransactions() != null)
                        .flatMap(e -> e.getTransactions().stream())
                        .map(e -> (Transaction) e.get())
                        .map(Transaction::getHash))
                // group in batches
                .group()
                .intoLists()
                // TODO this batching it working ber block batch. So it will not help with small amound of tx per block
                .of(config.receiptBatch())
                .onItem()
                .transformToUniAndConcatenate(group -> {
                    // execute batch TransactionReceipt for each block transaction from blocks batch
                    log.infof("Receipts:%s[%s-%s]", group.size(), group.getFirst(), group.getLast());
                    BatchRequest batch = eth.newBatch();
                    for (String trx : group) {
                        batch.add(eth.ethGetTransactionReceipt(trx));
                    }
                    return RetryUtil.retryBatch(log, "receipts(size:%s %s-%s)".formatted(group.size(), group.getFirst(), group.getLast()), config, batch.sendAsync());
                })
                .collect()
                .asList()
                .map(all -> {
                    // block hash to list of receipts in block
                    Map<String, List<TransactionReceipt>> receipts = all.stream()
                            .flatMap(e -> e.getResponses().stream())
                            .map(e -> (TransactionReceipt) e.getResult())
                            .filter(Objects::nonNull)
                            .collect(Collectors.groupingBy(TransactionReceipt::getBlockHash));
                    blocks.stream()
                            .filter(e -> e.getBlock() != null)
                            .forEach(b -> {
                                List<TransactionReceipt> r = receipts.get(b.getBlock().getHash());
                                if (r != null) {
                                    b.setReceipts(BlockAndReceiptsWrapper.mapReceipts(r));
                                }
                            });
                    return blocks;
                });
    }

    private Uni<List<BlockAndReceiptsWrapper>> contracts(List<BlockAndReceiptsWrapper> blocks) {
    }
}

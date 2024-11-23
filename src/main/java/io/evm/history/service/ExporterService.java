package io.evm.history.service;

import io.evm.history.config.EvmHistoryConfig;
import io.evm.history.db.dao.TransactionDataDao;
import io.evm.history.db.model.TransactionData;
import io.evm.history.service.model.BlockAndReceiptsWrapper;
import io.quarkus.runtime.StartupEvent;
import io.smallrye.config.Priorities;
import io.smallrye.mutiny.Uni;
import jakarta.annotation.Priority;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.event.Observes;
import jakarta.inject.Inject;
import lombok.extern.jbosslog.JBossLog;
import org.web3j.protocol.core.methods.response.EthBlock;
import org.web3j.protocol.core.methods.response.Transaction;
import org.web3j.protocol.core.methods.response.TransactionReceipt;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@JBossLog
@ApplicationScoped
public class ExporterService {

    @Inject
    EvmHistoryConfig config;
    @Inject
    BlockAndReceiptsService txService;
    @Inject
    TransactionDataDao txDao;

    void onStart(@Observes @Priority(Priorities.LIBRARY) StartupEvent ev) {
        txDao.template().await().indefinitely();
    }

    public Uni<Void> export() {
        //noinspection ReactiveStreamsUnusedPublisher
        return txDao.findLastBlockNumber()
                .onItem()
                // !!! re fetching last block, because it can be persisted not fully
                .transformToMulti(lastBlock -> txService.data(lastBlock))
                .group()
                .intoLists()
                .of(config.persistBatch())
                .call(this::persist)
                .onItem().ignoreAsUni();
    }

    protected Uni<Void> persist(List<BlockAndReceiptsWrapper> blocks) {
        List<TransactionData> persist = new ArrayList<>();
        for (BlockAndReceiptsWrapper data : blocks) {
            EthBlock.Block block = data.getBlock();
            if (block != null) {
                for (EthBlock.TransactionResult<?> tr : block.getTransactions()) {
                    Transaction tx = (Transaction) tr.get();
                    if (tx != null) {
                        Optional<TransactionReceipt> receipt = Optional.ofNullable(data.getReceipts())
                                .map(r -> data.getReceipts().get(tx.getTransactionIndexRaw()));
                        persist.add(new TransactionData(data.timestamp(), block, tx, receipt.orElse(null)));
                    }
                }
            }
        }
        if (!persist.isEmpty()) {
            log.infof("Persist:%s[%s-%s]", persist.size(), blocks.getFirst().getBlock().getNumber(), blocks.getLast()
                    .getBlock()
                    .getNumber());
            return txDao.bulk(persist).replaceWithVoid();
        } else {
            return Uni.createFrom().voidItem();
        }
    }
}

package io.evm.history.service;

import io.evm.history.config.EvmHistoryConfig;
import io.evm.history.db.dao.TransactionDataDao;
import io.evm.history.db.model.TransactionData;
import io.evm.history.service.model.TransactionReceiptContractWrapper;
import io.quarkus.runtime.StartupEvent;
import io.smallrye.config.Priorities;
import io.smallrye.mutiny.Uni;
import jakarta.annotation.Priority;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.event.Observes;
import jakarta.inject.Inject;
import lombok.extern.jbosslog.JBossLog;

import java.util.ArrayList;
import java.util.List;

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

    protected Uni<Void> persist(List<TransactionReceiptContractWrapper> txs) {
        List<TransactionData> persist = new ArrayList<>();
        for (TransactionReceiptContractWrapper tx : txs) {
            persist.add(new TransactionData(tx.timestamp(), tx.getBlock(), tx.getTx(), tx.getContractCode()));
        }
        if (!persist.isEmpty()) {
            log.infof("Persist:%s[%s-%s]", persist.size(), txs.getFirst().getBlock().getNumber(), txs.getLast()
                    .getBlock()
                    .getNumber());
            return txDao.bulk(persist).replaceWithVoid();
        } else {
            return Uni.createFrom().voidItem();
        }
    }
}

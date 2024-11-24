package io.evm.history.service;

import io.evm.history.config.EvmHistoryConfig;
import io.evm.history.db.dao.ContractDataDao;
import io.evm.history.db.dao.TransactionDataDao;
import io.evm.history.db.model.ContractData;
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

import java.math.BigInteger;
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
    @Inject
    ContractDataDao contractDao;

    void onStart(@Observes @Priority(Priorities.LIBRARY) StartupEvent ev) {
        txDao.template().await().indefinitely();
    }

    public Uni<Void> export() {
        //noinspection ReactiveStreamsUnusedPublisher
        return txDao.findLastBlockNumber()
                .onItem()
                // !!! re fetching last block, because it can be persisted not fully
                .transformToMulti(lastBlock -> txService.data(lastBlock == null ? null : BigInteger.valueOf(lastBlock)))
                .group()
                .intoLists()
                .of(config.persistBatch())
                .call(this::persist)
                .onItem().ignoreAsUni();
    }

    protected Uni<Void> persist(List<TransactionReceiptContractWrapper> data) {
        List<TransactionData> transactions = new ArrayList<>();
        List<ContractData> contracts = new ArrayList<>();
        for (TransactionReceiptContractWrapper tx : data) {
            transactions.add(new TransactionData(tx));
            if (tx.getReceipt() != null && tx.isCreateContract()) {
                // contract persisted with first transaction in batch, where it is found
                contracts.add(new ContractData(tx));
            }
        }
        if (!transactions.isEmpty()) {
            log.infof("Persist:%s[%s-%s]", transactions.size() + contracts.size(), data.getFirst()
                    .getBlock()
                    .getNumber(), data.getLast().getBlock().getNumber());
            return txDao.mBulk(List.of(txDao.bulkOpIndex(transactions), contractDao.bulkOpIndex(contracts)))
                    .replaceWithVoid();
        } else {
            return Uni.createFrom().voidItem();
        }
    }
}

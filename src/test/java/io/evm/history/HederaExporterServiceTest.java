package io.evm.history;

import co.elastic.clients.elasticsearch._types.Refresh;
import io.evm.history.db.dao.TransactionDataDao;
import io.evm.history.service.ExporterService;
import io.quarkus.test.junit.QuarkusTest;
import jakarta.inject.Inject;
import lombok.extern.jbosslog.JBossLog;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

@JBossLog
@QuarkusTest
public class HederaExporterServiceTest {

    @Inject
    ExporterService service;
    @Inject
    TransactionDataDao txDao;

    @Test
    public void exportTest() {
        service.export(Refresh.WaitFor)
                .onFailure().invoke(log::error)
                .await().indefinitely();
        Assertions.assertTrue(txDao.findLastBlockNumber().await().indefinitely() > 9884463);
    }
}

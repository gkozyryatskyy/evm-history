package io.evm.history;

import io.evm.history.service.ExporterService;
import io.quarkus.test.junit.QuarkusTest;
import jakarta.inject.Inject;
import lombok.extern.jbosslog.JBossLog;
import org.junit.jupiter.api.*;

@JBossLog
@QuarkusTest
public class HederaExporterServiceTest {

    @Inject
    ExporterService service;

    @Test
    public void exportTest() {
        service.export()
                .onFailure().invoke(log::error)
                .await().indefinitely();
    }
}

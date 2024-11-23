package io.evm.history;

import io.evm.history.config.EvmHistoryConfig;
import io.evm.history.service.ExporterService;
import io.quarkus.runtime.Quarkus;
import io.quarkus.runtime.QuarkusApplication;
import io.quarkus.runtime.annotations.QuarkusMain;
import jakarta.inject.Inject;
import org.jboss.logging.Logger;

import java.util.Arrays;
import java.util.concurrent.atomic.AtomicInteger;

@QuarkusMain
public class Main {

    private static final Logger log = Logger.getLogger(Main.class.getName());

    public static void main(String... args) {
        Runtime runtime = Runtime.getRuntime();
        log.infof("Running from main method. CPU:[%s],MEM.total:[%s],MEM.max:[%s] Args:%s",
                runtime.availableProcessors(),
                runtime.totalMemory(),
                runtime.maxMemory(),
                Arrays.toString(args));
        Quarkus.run(EvmHistoryExporter.class, args);
    }

    public static class EvmHistoryExporter implements QuarkusApplication {

        @Inject
        EvmHistoryConfig config;
        @Inject
        ExporterService service;

        @Override
        public int run(String... args) {
            AtomicInteger exitCode = new AtomicInteger(0);
            if (config.enabled()) {
                service.export()
                        .subscribe()
                        .with(e -> log.info("BlockAndReceiptsService.export() is finished!"),
                                e -> {
                                    log.error("BlockAndReceiptsService.export() error.", e);
                                    exitCode.set(1);
                                    Quarkus.asyncExit(exitCode.get());
                                });
            }
            Quarkus.waitForExit();
            return exitCode.get();
        }
    }
}

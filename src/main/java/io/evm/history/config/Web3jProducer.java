package io.evm.history.config;

import jakarta.annotation.PreDestroy;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.inject.Produces;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import org.jboss.logging.Logger;
import org.web3j.protocol.Web3j;
import org.web3j.protocol.http.HttpService;

import java.util.concurrent.TimeUnit;

@ApplicationScoped
public class Web3jProducer {

    private final Logger log = Logger.getLogger(this.getClass());

    @Inject
    Web3jConfig config;

    private Web3j web3;

    @Produces
    @Singleton
    public Web3j web3() {
        log.info("Initialising Web3j client. host:" + config.host());
        this.web3 = Web3j.build(new HttpService(
                config.host(),
                HttpService.getOkHttpClientBuilder()
                        .connectTimeout(config.connectTimeout(), TimeUnit.MILLISECONDS)
                        .writeTimeout(config.writeTimeout(), TimeUnit.MILLISECONDS)
                        .readTimeout(config.readTimeout(), TimeUnit.MILLISECONDS)
                        .build()
        ));
        return this.web3;
    }

    @PreDestroy
    void destroy() {
        if (this.web3 != null) {
            log.infof("Closing Web3j client. host:" + config.host());
            this.web3.shutdown();
        }
    }
}

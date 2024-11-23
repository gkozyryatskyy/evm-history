package io.evm.history.config;

import io.smallrye.config.ConfigMapping;
import io.smallrye.config.WithDefault;

@ConfigMapping(prefix = "evm.history.web3j")
public interface Web3jConfig {

    String host();

    @WithDefault("10000")
    long connectTimeout();

    @WithDefault("10000")
    long writeTimeout();

    @WithDefault("60000")
    long readTimeout();
}

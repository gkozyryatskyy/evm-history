package io.evm.history.config;

import io.evm.history.config.core.RetryConfig;
import io.smallrye.config.ConfigMapping;
import io.smallrye.config.WithDefault;

import java.math.BigInteger;
import java.util.Optional;

@ConfigMapping(prefix = "evm.history.export")
public interface EvmHistoryConfig {

    RetryConfig retry();
    @WithDefault("true")
    boolean enabled();
    BigInteger blockFrom();
    BigInteger blockTo();
    @WithDefault("100") // amount of blocks to request from RPC node
    int blockBatch();
    @WithDefault("100") // amount of receipts to request from RPC node
    int receiptBatch();
    @WithDefault("100") // amount of contracts to request from RPC node
    int contractBatch();
    @WithDefault("100") // amount of blocks to persist
    int persistBatch();
    Optional<Long> throttling();

}

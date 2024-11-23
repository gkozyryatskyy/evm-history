package io.evm.history.config.core;

import io.smallrye.config.WithDefault;

public interface RetryConfig {

    @WithDefault("5000")
    int initialBackoff();
    @WithDefault("60000")
    int maxBackoff();
    @WithDefault("0.2")
    double jitter();
    @WithDefault("10")
    int atMost();
}

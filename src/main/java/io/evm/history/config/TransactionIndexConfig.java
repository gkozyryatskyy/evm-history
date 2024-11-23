package io.evm.history.config;

import io.evm.history.config.core.EsIndexConfig;
import io.smallrye.config.ConfigMapping;

@ConfigMapping(prefix = "evm.history.tx")
public interface TransactionIndexConfig extends EsIndexConfig {
}

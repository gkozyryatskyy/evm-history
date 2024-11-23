package io.evm.history.service.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import org.web3j.protocol.core.methods.response.EthBlock;

@Getter
@Setter
@AllArgsConstructor
public class BlockWrapper {

    private final EthBlock.Block block;

    public long timestamp() {
        return block.getTimestamp().longValue() * 1000;
    }
}

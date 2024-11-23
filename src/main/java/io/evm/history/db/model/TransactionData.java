package io.evm.history.db.model;

import io.evm.history.db.model.core.ITimeSeries;
import lombok.Getter;
import org.web3j.protocol.core.methods.response.EthBlock;
import org.web3j.protocol.core.methods.response.Transaction;

import java.math.BigInteger;

@Getter
public class TransactionData implements ITimeSeries {

    private final Long ts;
    private final BigInteger blockNumber;
    //TODO

    public TransactionData(long ts, EthBlock.Block block, Transaction tx, String code) {
        this.ts = ts;
        this.blockNumber = block.getNumber();
    }
}

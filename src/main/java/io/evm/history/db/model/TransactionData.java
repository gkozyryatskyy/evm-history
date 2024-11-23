package io.evm.history.db.model;

import io.evm.history.db.model.core.ITimeSeries;
import lombok.Getter;
import org.web3j.protocol.core.methods.response.EthBlock;
import org.web3j.protocol.core.methods.response.Transaction;
import org.web3j.protocol.core.methods.response.TransactionReceipt;

import java.math.BigInteger;

@Getter
public class TransactionData implements ITimeSeries {

    private Long ts;
    private BigInteger blockNumber;
    //TODO

    public TransactionData(long ts, EthBlock.Block block, Transaction tx, TransactionReceipt receipt) {
        this.ts = ts;
        this.blockNumber = block.getNumber();
    }
}

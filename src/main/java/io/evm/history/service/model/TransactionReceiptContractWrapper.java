package io.evm.history.service.model;

import lombok.Getter;
import lombok.Setter;
import org.web3j.protocol.core.methods.response.EthBlock;
import org.web3j.protocol.core.methods.response.Transaction;
import org.web3j.protocol.core.methods.response.TransactionReceipt;

@Getter
@Setter
public class TransactionReceiptContractWrapper extends BlockWrapper {

    private final Transaction tx;
    private TransactionReceipt receipt;
    private Integer codeBytesLength;

    public TransactionReceiptContractWrapper(EthBlock.Block block, Transaction tx) {
        super(block);
        this.tx = tx;
    }

}

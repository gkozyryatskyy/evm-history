package io.evm.history.db.model;

import io.evm.history.db.model.core.ITimeSeries;
import io.evm.history.service.model.TransactionReceiptContractWrapper;
import io.evm.history.util.RawUtil;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
@NoArgsConstructor
public class TransactionData implements ITimeSeries {

    // block
    private Long ts;
    private Long blockNumber;
    private String blockHash;
    // tx
    private String txHash;
    private String value;
    private String gas;
    private String input;
    private String type;
    // receipt
    private String contractAddress;
    private Long gasUsed;
    private String from;
    private String to;
    // contract
    private Integer codeBytesLength;

    public TransactionData(TransactionReceiptContractWrapper data) {
        // block
        this.ts = data.timestamp();
        this.blockNumber = data.getBlock().getNumber().longValue();
        this.blockHash = data.getBlock().getHash();
        // tx
        this.txHash = data.getTx().getHash();
        this.value = data.getTx().getValueRaw();
        this.gas = data.getTx().getGasRaw();
        this.input = data.getTx().getInput();
        this.type = data.getTx().getType();
        // receipt
        this.contractAddress = data.getReceipt().getContractAddress();
        this.gasUsed = RawUtil.bigint(data.getReceipt()::getGasUsedRaw, data.getReceipt()::getGasUsed).longValue();
        this.from = data.getReceipt().getFrom();
        this.to = data.getReceipt().getTo();
        if (data.getCodeBytesLength() != null) {
            this.codeBytesLength = data.getCodeBytesLength();
        }
    }
}

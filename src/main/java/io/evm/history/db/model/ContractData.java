package io.evm.history.db.model;

import io.evm.history.db.model.core.ITimeSeries;
import io.evm.history.service.model.TransactionReceiptContractWrapper;
import lombok.Getter;

@Getter
public class ContractData implements ITimeSeries {

    private final Long ts;
    private final String address;
    private final int codeBytesLength;

    public ContractData(TransactionReceiptContractWrapper data) {
        this.ts = data.timestamp();
        this.address = data.getReceipt().getContractAddress();
        this.codeBytesLength = data.getCodeBytesLength();
    }
}

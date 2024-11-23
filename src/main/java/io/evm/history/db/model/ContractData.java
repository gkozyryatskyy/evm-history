package io.evm.history.db.model;

import io.evm.history.db.model.core.ITimeSeries;
import io.evm.history.service.model.TransactionReceiptContractWrapper;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class ContractData implements ITimeSeries {

    private Long ts;
    private String address;
    private int codeBytesLength;

    public ContractData(TransactionReceiptContractWrapper data) {
        this.ts = data.timestamp();
        this.address = data.getReceipt().getContractAddress();
        this.codeBytesLength = data.getCodeBytesLength();
    }
}

package io.evm.history.db.model;

import io.evm.history.db.model.core._Timeseries;

public interface _TransactionData extends _Timeseries {

    // block
    String blockNumber = "blockNumber";
    String blockHash = "blockHash";
    // tx
    String txHash = "txHash";
    String value = "value";
    String gas = "gas";
    String input = "input";
    String type = "type";
    // receipt
    String contractAddress = "contractAddress";
    String gasUsed = "gasUsed";
    String from = "from";
    String to = "to";
    // contract
    String codeBytesLength = "codeBytesLength";
}

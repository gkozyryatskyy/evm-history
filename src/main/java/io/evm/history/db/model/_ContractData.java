package io.evm.history.db.model;

import io.evm.history.db.model.core._Timeseries;

public interface _ContractData extends _Timeseries {

    String address = "address";
    String codeBytesLength = "codeBytesLength";
}

package io.evm.history.util;

import lombok.experimental.UtilityClass;

import java.math.BigInteger;
import java.util.function.Supplier;

@UtilityClass
public class RawUtil {

    public BigInteger bigint(Supplier<String> raw, Supplier<BigInteger> bigint) {
        if (raw.get() != null) {
            return bigint.get();
        } else {
            return null;
        }
    }
}

package io.evm.history.util.block;

import lombok.AllArgsConstructor;
import org.jetbrains.annotations.NotNull;

import java.math.BigInteger;
import java.util.Iterator;

@AllArgsConstructor
public class IterableBlocks implements Iterable<BigInteger> {

    private BigInteger from;
    private BigInteger to;

    @NotNull
    @Override
    public Iterator<BigInteger> iterator() {
        return new BlocksIterator(from, to);
    }
}

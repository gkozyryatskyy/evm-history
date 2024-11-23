package io.evm.history.util.block;

import lombok.AllArgsConstructor;

import java.math.BigInteger;
import java.util.Iterator;
import java.util.NoSuchElementException;

@AllArgsConstructor
public class BlocksIterator implements Iterator<BigInteger> {

    private BigInteger cursor;
    private BigInteger to;

    @Override
    public boolean hasNext() {
        return cursor.compareTo(to) < 0;
    }

    @Override
    public BigInteger next() {
        BigInteger temp = cursor;
        if (temp.compareTo(to) >= 0)
            throw new NoSuchElementException();
        cursor = temp.add(BigInteger.ONE);
        return temp;
    }
}

package com.olacabs.fabric.compute.tracking;

import java.util.Arrays;

/**
 * Created by santanu.s on 11/09/15.
 */
public class SimpleBitSet {
    private static final int NUM_BITS_IN_WORD = Long.BYTES * 8;
    private final int nBits;
    private int numWords;
    private long words[];

    public SimpleBitSet(int nBits) {
        assert (nBits > 0);
        this.nBits = nBits;
        this.numWords = (nBits + NUM_BITS_IN_WORD - 1) / NUM_BITS_IN_WORD;
        words = new long[numWords];
        Arrays.fill(words, 0);
    }

    public void set(int index) {
        int pos = (index / NUM_BITS_IN_WORD);
        long bits = index - (pos * numWords);
        long mask = 0x01L << bits;
        words[pos] |= mask;
    }

    public void unset(int index) {
        int pos = (index / NUM_BITS_IN_WORD);
        long bits = index - (pos * numWords);
        long mask = ~(0x01L << bits);
        words[pos] &= mask;
    }

    public int cardinality() {
        int count = 0;
        for(int i = 0; i < numWords; i++) {
            count += Long.bitCount(words[i]);
        }
        return count;
    }

    public boolean hasSetBits() {
        boolean anyBitsSet = false;
        for (int i = 0; i < numWords; i++) {
            anyBitsSet = anyBitsSet || (words[i] != 0);
            if (anyBitsSet) break;
        }
        return anyBitsSet;
    }
}

package com.olacabs.fabric.compute.tracking;

import org.junit.Assert;
import org.junit.Test;


/**
 * Created by santanu.s on 11/09/15.
 */
public class SimpleBitSetTest {
    private int numBits = 256;
    private SimpleBitSet bitSet = new SimpleBitSet(numBits);

    @Test
    public void check() {
        for(int i = 0; i < numBits; i++) {
            bitSet.set(i);
        }
        Assert.assertEquals(numBits, bitSet.cardinality());
        for(int i = 0; i < numBits; i+=2) {
            bitSet.unset(i);
        }
        Assert.assertEquals(numBits >> 1, bitSet.cardinality());

    }

    @Test
    public void testHasSetBits() {
        bitSet.set(10);
        Assert.assertEquals(true, bitSet.hasSetBits());
        bitSet.unset(10);
        Assert.assertEquals(false, bitSet.hasSetBits());
    }

}
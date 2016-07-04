/*
 * Copyright 2016 ANI Technologies Pvt. Ltd.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.olacabs.fabric.compute.tracking;

import org.junit.Assert;
import org.junit.Test;

public class SimpleBitSetTest {
    private int numBits = 256;
    private SimpleBitSet bitSet = new SimpleBitSet(numBits);

    @Test
    public void check() {
        for (int i = 0; i < numBits; i++) {
            bitSet.set(i);
        }
        Assert.assertEquals(numBits, bitSet.cardinality());
        for (int i = 0; i < numBits; i += 2) {
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
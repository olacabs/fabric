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

package com.olacabs.fabric.compute.sources.kafka;

import kafka.producer.Partitioner;
import kafka.utils.VerifiableProperties;

import java.util.Random;
import java.util.concurrent.ThreadLocalRandom;

public class CustomPartitioner implements Partitioner {
    private Random random = ThreadLocalRandom.current();

    public CustomPartitioner(VerifiableProperties properties) {
    }

    @Override
    public int partition(Object o, int i) {
        int p = random.nextInt(3);
        System.out.println("Sending to " + p);
        return p;
    }
}

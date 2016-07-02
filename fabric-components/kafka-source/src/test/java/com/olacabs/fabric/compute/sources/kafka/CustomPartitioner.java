package com.olacabs.fabric.compute.sources.kafka;

import kafka.producer.Partitioner;
import kafka.utils.VerifiableProperties;

import java.util.Random;
import java.util.concurrent.ThreadLocalRandom;

/**
 * Created by santanu.s on 02/11/15.
 */
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

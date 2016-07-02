package com.olacabs.fabric.compute.pipeline;

/**
 * Created by santanu.s on 12/09/15.
 */
public interface TransactionIdGenerator {
    void seed(long seed);
    long transactionId();
}

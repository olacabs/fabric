package com.olacabs.fabric.compute.pipeline;

import java.util.concurrent.atomic.AtomicLong;

/**
 * Created by santanu.s on 12/09/15.
 */
public class SourceIdBasedTransactionIdGenerator implements TransactionIdGenerator {
    private AtomicLong transactionId = new AtomicLong(0L);

    private final MessageSource messageSource;

    public SourceIdBasedTransactionIdGenerator(MessageSource messageSource) {
        this.messageSource = messageSource;
    }

    @Override
    public void seed(long seed) {
        transactionId.set(seed);
    }

    @Override
    public long transactionId() {
        return transactionId.getAndIncrement() | ((long)(messageSource.communicationId()) << ((Long.BYTES - 1) * 8));
    }
}

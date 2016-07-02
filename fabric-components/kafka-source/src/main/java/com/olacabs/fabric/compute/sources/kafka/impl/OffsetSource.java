package com.olacabs.fabric.compute.sources.kafka.impl;

/**
 * Created by santanu.s on 07/10/15.
 */
public interface OffsetSource {
    void saveOffset(final String topic, int partition, long offset) throws Exception;

    long startOffset(final String topic, int partition) throws Exception;
}

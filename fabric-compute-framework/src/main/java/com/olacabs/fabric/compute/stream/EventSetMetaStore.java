package com.olacabs.fabric.compute.stream;

import com.olacabs.fabric.model.event.EventSetMeta;

import java.util.Collection;

/**
 * Created by santanu.s on 10/09/15.
 */
public interface EventSetMetaStore {
    void save(EventSetMeta eventSetMeta);
    void ack(long transactionId);
    EventSetMeta get(long transactionId);
    Collection<EventSetMeta> get();
}

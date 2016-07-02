package com.olacabs.fabric.compute.stream.impl;

import com.olacabs.fabric.compute.stream.EventSetMetaStore;
import com.olacabs.fabric.model.event.EventSetMeta;

import java.util.Collection;
import java.util.Collections;

/**
 * Created by santanu.s on 10/09/15.
 */
public class InMemoryEventSetMetaStore implements EventSetMetaStore {

    //private Map<Long, EventSetMeta> eventSetMetaMap = Maps.newHashMap();

    @Override
    public void save(EventSetMeta eventSetMeta) {
        //eventSetMetaMap.put(eventSetMeta.getTransactionId(), eventSetMeta);
    }

    @Override
    public void ack(long transactionId) {
        //eventSetMetaMap.remove(transactionId);
    }

    @Override
    public EventSetMeta get(long transactionId) {
        return null;
        //return eventSetMetaMap.get(transactionId);
    }

    @Override
    public Collection<EventSetMeta> get() {
        //return eventSetMetaMap.values();
        return Collections.emptyList();
    }
}
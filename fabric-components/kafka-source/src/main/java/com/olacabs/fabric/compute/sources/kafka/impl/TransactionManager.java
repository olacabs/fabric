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

package com.olacabs.fabric.compute.sources.kafka.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.Queues;
import com.olacabs.fabric.model.event.EventSetMeta;
import lombok.Builder;
import org.apache.curator.framework.CuratorFramework;
import org.apache.zookeeper.KeeperException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Queue;
import java.util.stream.Collectors;

/**
 * TODO java doc.
 */
public class TransactionManager {
    private static final Logger LOGGER = LoggerFactory.getLogger(TransactionManager.class);

    private String topologyName;
    private String topic;
    private int partition;
    private ObjectMapper mapper;
    private CuratorFramework curator;
    private Queue<EventSetMeta> existingEvents = Queues.newArrayDeque();
    private long lastTransactionId = 0L;

    @Builder
    public TransactionManager(String topologyName, String topic, int partition, ObjectMapper mapper,
            CuratorFramework curator) {
        this.topologyName = topologyName;
        this.topic = topic;
        this.partition = partition;
        this.mapper = mapper;
        this.curator = curator;
    }

    public void save(EventSetMeta eventSetMeta) throws Exception {
        curator.create().creatingParentContainersIfNeeded()
            .forPath(txnPath(eventSetMeta.getTransactionId()), mapper.writeValueAsBytes(eventSetMeta));
        //This is okay, as this object is accessed always from only a single thread.
        //So this is equivalent to saving lastTansactionId
        saveLastTransactionId(eventSetMeta.getTransactionId());
    }

    public void ack(long transactionId) throws Exception {
        try {
            curator.delete().forPath(txnPath(transactionId));
        } catch (KeeperException.NoNodeException e) {
            //This error might happen as, the control might have gone to some other node
            LOGGER.warn("No node exists for [{}:{}} txn {}. Probably acked by another instance.", topic, partition,
                    transactionId);
        }
        if (!existingEvents.isEmpty()) {
            if (existingEvents.peek().getTransactionId() == transactionId) {
                existingEvents.remove();
            } else {
                LOGGER.warn("[{}:{}] Received ack for a non-existing transaction even though there are existing events!"
                        + " Ordering might have been screwed!", topic, partition);
            }
        }
    }

    public EventSetMeta getExisting() {
        if (!existingEvents.isEmpty()) {
            return existingEvents.peek();
        }
        return null;
    }

    public long getNewTransactionId() {
        return ++lastTransactionId;
    }

    public void readPendingTransactions() throws Exception {
        lastTransactionId = readLastTransactionId();
        LOGGER.info("[{}:{}] Last transaction id set to: {}", topic, partition, lastTransactionId);
        List<EventSetMeta> eventSetMetas = readExistingTransactions();
        if (eventSetMetas.isEmpty()) {
            LOGGER.info("No pending transactions on partition [{}:{}]", topic, partition);
        } else {
            existingEvents.addAll(eventSetMetas);
            LOGGER.info("Queued up {} pending transactions for [{}:{}]", eventSetMetas.size(), topic, partition);
        }
    }

    public void initialize() throws Exception {
        try {
            if (null == curator.checkExists().forPath(txnIdPath())) {
                curator.create().creatingParentContainersIfNeeded()
                        .forPath(txnIdPath(), ByteBuffer.allocate(Long.BYTES).putLong(0L).array());
                lastTransactionId = 0L;
            }
        } catch (KeeperException.NodeExistsException e) {
            LOGGER.info("Txn ID path exists: " + path());
        }
        try {
            curator.create().creatingParentContainersIfNeeded().forPath(path());
        } catch (KeeperException.NodeExistsException e) {
            LOGGER.info("Txn path exists: " + path());
        }

    }

    private void saveLastTransactionId(long transactionId) throws Exception {
        curator.setData().forPath(txnIdPath(), ByteBuffer.allocate(Long.BYTES).putLong(transactionId).array());
    }

    private long readLastTransactionId() throws Exception {
        byte[] data = curator.getData().forPath(txnIdPath());
        ByteBuffer read = ByteBuffer.allocate(Long.BYTES).put(data, 0, data.length);
        read.flip();
        return read.getLong();
    }

    private String path() {
        return String.format("/%s/%s/transactions/%d", topologyName, topic, partition);
    }

    private String txnPath(long txnId) {
        return txnPath(Long.toString(txnId));
    }

    private String txnPath(String txnId) {
        return String.format("%s/%s", path(), txnId);
    }

    private List<EventSetMeta> readExistingTransactions() throws Exception {
        final String txnLocation = path();
        List<String> txnIds = curator.getChildren().forPath(txnLocation);
        Collections.sort(txnIds, (lhs, rhs) -> Long.compare(Long.parseLong(lhs), Long.parseLong(rhs)));
        return txnIds.stream().map(txnId -> {
            try {
                return mapper.readValue(curator.getData().forPath(txnPath(txnId)), EventSetMeta.class);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }).collect(Collectors.toCollection(ArrayList::new));
    }

    private String txnIdPath() {
        return String.format("/%s/%s/transactionId/%d", topologyName, topic, partition);
    }
}

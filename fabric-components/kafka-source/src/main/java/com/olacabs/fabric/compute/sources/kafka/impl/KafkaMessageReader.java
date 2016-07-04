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

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Maps;
import com.olacabs.fabric.model.event.Event;
import com.olacabs.fabric.model.event.EventSetMeta;
import com.olacabs.fabric.model.event.RawEventBundle;
import kafka.api.FetchRequest;
import kafka.api.FetchRequestBuilder;
import kafka.common.OffsetOutOfRangeException;
import kafka.javaapi.FetchResponse;
import kafka.javaapi.consumer.SimpleConsumer;
import kafka.message.MessageAndOffset;
import lombok.Builder;
import lombok.Getter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.ByteBuffer;
import java.nio.channels.ClosedChannelException;
import java.util.Map;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

public class KafkaMessageReader implements Runnable {
    private static final Logger logger = LoggerFactory.getLogger(KafkaMessageReader.class);
    private static final int DEFAULT_TIME_TO_WAIT = 30000;
    private static final int DEFAULT_MIN_BYTES_TO_WAIT = 1;

    private final String topic;
    @Getter
    private final int partition;
    private final KafkaConsumerBuilder consumerBuilder;
    private final OffsetSource offsetSource;
    private final BlockingQueue<RawEventBundle> events;
    private final int bufferSize;
    private final TransactionManager transactionManager;

    private SimpleConsumer consumer;
    private AtomicBoolean lastMessageAcked = new AtomicBoolean(true);
    private AtomicBoolean stopRequested = new AtomicBoolean();
    private ReentrantLock lock = new ReentrantLock();
    private Condition condition = lock.newCondition();
    private String startOffsetPickStrategy;

    @Builder
    public KafkaMessageReader(String topic, int partition, KafkaConsumerBuilder consumerBuilder, OffsetSource offsetSource, BlockingQueue<RawEventBundle> events, int bufferSize, TransactionManager transactionManager, String startOffsetPickStrategy) {
        this.topic = topic;
        this.partition = partition;
        this.consumerBuilder = consumerBuilder;
        this.offsetSource = offsetSource;
        this.events = events;
        this.bufferSize = bufferSize;
        this.transactionManager = transactionManager;
        this.startOffsetPickStrategy = startOffsetPickStrategy;
    }

    public void start() throws Exception {
        transactionManager.initialize();
    }

    public RawEventBundle readEvents(long startOffset, int bufferSize) throws Exception {
        ImmutableList.Builder<Event> eventsBuilder = ImmutableList.builder();
        Map<String, Object> meta = Maps.newHashMap();
        long minOffset = -1;
        long nextMessageOffset = 0;
        boolean hasMessages = false;
        EventSetMeta existingMeta = null;
        long transactionId = 0L;
        do {
            if (stopRequested.get()) {
                logger.info("[{}:{}] Exiting as stop was requested...", topic, partition);
                throw new Exception("Stop called as we lost leadership");
            }
            try {
                if (null == consumer) {
                    consumer = consumerBuilder.consumersForTopicPartition(topic, partition);
                    logger.info("Created consumers for [{}:{}]", topic, partition);
                }
                existingMeta = transactionManager.getExisting();
                int bufferSizeInUse = bufferSize;
                if (null != existingMeta) {
                    logger.info("[{}:{}] Found existing transaction: {}", topic, partition, existingMeta.getTransactionId());
                    startOffset = Long.parseLong(existingMeta.getMeta().get("startOffset").toString());
                    bufferSizeInUse = Integer.parseInt(existingMeta.getMeta().get("size").toString());
                    bufferSizeInUse = (0 == bufferSizeInUse) ? bufferSize : bufferSizeInUse;
                }
                if (-1 == startOffset) {
                    logger.warn("[{}:{}] No offset found from store. Will read current offsets from kafka. " +
                            "Unprocessed messages before that will be lost. This is normal for first time.",
                        topic, partition, startOffset);
                    if (startOffsetPickStrategy.equalsIgnoreCase(StartOffsetPickStrategy.EARLIEST.toString())) {
                        logger.debug("Picking earliest available offset for partition from Kafka");
                        startOffset = KafkaMetadataClient.startOffset(consumer,
                            topic, partition, StartOffsetPickStrategy.EARLIEST.toString());
                    } else {
                        logger.debug("Picking latest available offset for partition from Kafka");
                        startOffset = KafkaMetadataClient.startOffset(consumer,
                            topic, partition, StartOffsetPickStrategy.LATEST.toString());
                    }
                    logger.info("[{}:{}] Start offset set from kafka meta to: {}", topic, partition, startOffset);
                } else {
                    logger.info("[{}:{}] Start offset set to: {}", topic, partition, startOffset);
                }
                FetchRequest fetchRequest = new FetchRequestBuilder()
                    .clientId(HostUtils.hostname())
                    .addFetch(topic, partition, startOffset, bufferSizeInUse)
                    .maxWait(DEFAULT_TIME_TO_WAIT)
                    .minBytes(DEFAULT_MIN_BYTES_TO_WAIT)
                    .build();
                FetchResponse fetchResponse = consumer.fetch(fetchRequest);

                if (fetchResponse.hasError()) {
                    short errorCode = fetchResponse.errorCode(topic, partition);
                    if (1 == errorCode) {
                        logger.warn("[{}:{}] Has lagged too much. Offsets will be reset to earliest available message.",
                            topic, partition);
                        startOffset = -1;
                        if (null != existingMeta) {
                            logger.warn("[{}:{}] Transaction {} is too old and cannot be processed. Will be acked without processing.", topic, partition, existingMeta.getTransactionId());
                            transactionManager.ack(existingMeta.getTransactionId());
                        }
                        continue;
                    }
                    logger.error("[{}:{}] Received error-code from kafka: ", topic, partition, errorCode);
                    throw new Exception("Received error-code from kafka: " + errorCode);
                }
                for (MessageAndOffset messageAndOffset : fetchResponse.messageSet(topic, partition)) {
                    minOffset = (-1 == minOffset) ? messageAndOffset.offset() : minOffset;
                    nextMessageOffset = messageAndOffset.nextOffset();
                    ByteBuffer payload = messageAndOffset.message().payload();
                    byte[] bytes = new byte[payload.limit()];
                    payload.get(bytes);
                    eventsBuilder.add(Event.builder()
                        .id(messageAndOffset.offset())
                        .data(bytes)
                        .build());
                    hasMessages = true;
                }
                if (hasMessages) {
                    if (null == existingMeta) {
                        meta.put("topic", topic);
                        meta.put("partition", partition);
                        meta.put("startOffset", minOffset);
                        meta.put("endOffset", nextMessageOffset);
                        meta.put("size", fetchResponse.messageSet(topic, partition).sizeInBytes());
                        transactionId = transactionManager.getNewTransactionId();
                        logger.info("[{}:{} Transaction ID: {}", topic, partition, transactionId);
                        transactionManager.save(
                            EventSetMeta.builder()
                                .transactionId(transactionId)
                                .meta(meta)
                                .build());
                    } else {
                        transactionId = existingMeta.getTransactionId();
                        meta.putAll(existingMeta.getMeta());
                    }
                    break;
                } else {
                    logger.info("[{}:{}]No messages detected.", topic, partition);
                }
            } /*catch (SocketTimeoutException e) {
                logger.error("[{}:{}] Socket timed out without data", topic, partition);
            } *//*catch (BrokerQueryException| ClosedChannelException |InterruptedException e) {
                logger.error("[{}:{}] Interrupted. Not a leader anymore.", topic, partition);
                if(null != consumer) {
                    consumer.close();
                }
                consumer = null;
                throw e;
                //throw new Exception(String.format("Not a leader anymore: [%s:%d]", topic, partition));
            } */ catch (Throwable t) {
                if (t instanceof OffsetOutOfRangeException || t instanceof org.apache.kafka.common.errors.OffsetOutOfRangeException) {
                    startOffset = -1;
                    if (null != existingMeta) {
                        logger.warn("[{}:{}] Transaction {} is too old and cannot be processed. Will be acked without processing.", topic, partition, existingMeta.getTransactionId());
                        transactionManager.ack(existingMeta.getTransactionId());
                    }
                    logger.warn("[{}:{}] Has lagged too much. Offsets will be rest to earliest available message.", topic, partition);
                }
                logger.error("[{}:{}] Error occurred while reading message. Consumer will be recreated.", topic, partition, t);
                if (null != consumer) {
                    consumer.close();
                }
                consumer = null;
            }
            if (stopRequested.get()) {
                logger.info("[{}:{}] Fetched but exiting as stop was requested...", topic, partition);
                throw new Exception("Fetched, but exiting as we lost leadership");
            }
        } while (!hasMessages);
        logger.info("[{}:{}] Read data from {} to {}", topic, partition, startOffset, nextMessageOffset - 1);
        return RawEventBundle.builder()
            .events(eventsBuilder.build())
            .meta(ImmutableMap.copyOf(meta))
            .transactionId(transactionId)
            .partitionId(partition)
            .build();
    }

/*    public RawEventBundle replayEvents(Map<String, Object> meta) throws Exception {
        if(null == meta || meta.isEmpty()) {
            logger.warn("[{}:{}] replayEvents called with empty map. Reverting to getNewEvents", topic, partition);
            return getNewEvents();
        }
        long startOffset = -1;
        if(meta.containsKey("startOffset")) {
            startOffset = (long)meta.get("startOffset");
        }
        else {
            logger.warn("[{}:{}] replayEvents called without startOffset. Start offset will be set to 0.", topic, partition);
            //TODO:: SET TO 0 OR READ LAST OFFSET IN STORE
        }
        long endOffset = 0;
        if(meta.containsKey("endOffset")) {
            endOffset = (long)meta.get("endOffset");
        }
        else {
            logger.warn("[{}:{}] replayEvents called without endOffset. End offset will be set to -1.", topic, partition);
            //TODO:: SET TO 0 OR READ LAST OFFSET IN STORE
        }

        return readEvents(startOffset, bufferSize, endOffset);
    }*/

    public RawEventBundle getNewEvents() throws Exception {
        long startOffset = offsetSource.startOffset(topic, partition);
        return readEvents(startOffset, bufferSize);
    }

    public void ackMessage(RawEventBundle eventBundle) throws Exception {
        lock.lock();
        try {
            long endOffset = 0;
            if (eventBundle.getMeta().containsKey("endOffset")) {
                endOffset = Long.parseLong(eventBundle.getMeta().get("endOffset").toString());
            }
            offsetSource.saveOffset(topic, partition, endOffset);
            transactionManager.ack(eventBundle.getTransactionId());
            logger.info("[{}:{}]Messages acked till: {}", topic, partition, endOffset - 1);
            lastMessageAcked.set(true);
            condition.signal();
        } finally {
            lock.unlock();
        }
    }

    public void stop() {
        lock.lock();
        try {
            stopRequested.set(true);
            condition.signal();
        } finally {
            lock.unlock();
        }
        logger.info("[{}:{}] Stopped reader", topic, partition);
    }

    public void run() {
        logger.info("[{}:{}] Became leader.", topic, partition);
        logger.info("[{}:{}] Reading pending transactions", topic, partition);
        try {
            this.transactionManager.readPendingTransactions();
        } catch (Exception e) {
            logger.error("Error reading pending transactions...", topic, partition, e);
        }
        stopRequested.set(false);
        lastMessageAcked.set(true);
        while (true) {
            boolean messageReadingEnabled = false;
            try {
                lock.lock();
                while (!lastMessageAcked.get() && !stopRequested.get()) {
                    condition.await();
                }
                if (stopRequested.get()) {
                    logger.info("Stopping consumer");
                    if (null != consumer) {
                        consumer.close();
                    }
                    break;
                }
                if (lastMessageAcked.get()) {
                    lastMessageAcked.set(false);
                    messageReadingEnabled = true;
                }
            } catch (InterruptedException e) {
                logger.error("[{}:{}] Relinquishing leadership.", topic, partition);
            } catch (Throwable t) {
                logger.error("Blocking throwable: ", t);
            } finally {
                lock.unlock();
            }
            try {
                if (messageReadingEnabled) {
                    RawEventBundle eventBundle = getNewEvents();
                    events.put(eventBundle);
                }
            } catch (BrokerQueryException | ClosedChannelException | InterruptedException e) {
                logger.error("[{}:{}] Relinquishing leadership.", topic, partition);
                //return;
            } catch (Throwable t) {
                logger.error("Blocking throwable: ", t);
            }
        }
    }

}

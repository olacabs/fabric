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

package com.olacabs.fabric.compute.source;

import com.codahale.metrics.Histogram;
import com.codahale.metrics.MetricRegistry;
import com.codahale.metrics.annotation.Timed;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.github.rholder.retry.Retryer;
import com.github.rholder.retry.RetryerBuilder;
import com.github.rholder.retry.WaitStrategies;
import com.google.common.collect.Maps;
import com.olacabs.fabric.common.util.PropertyReader;
import com.olacabs.fabric.compute.ProcessingContext;
import com.olacabs.fabric.compute.pipeline.CommsIdGenerator;
import com.olacabs.fabric.compute.pipeline.MessageSource;
import com.olacabs.fabric.compute.pipeline.NotificationBus;
import com.olacabs.fabric.compute.pipeline.PipelineMessage;
import com.olacabs.fabric.compute.pipeline.SourceIdBasedTransactionIdGenerator;
import com.olacabs.fabric.compute.pipeline.TransactionIdGenerator;
import com.olacabs.fabric.model.common.ComponentMetadata;
import com.olacabs.fabric.model.event.EventSet;
import com.olacabs.fabric.model.event.RawEventBundle;
import io.astefanutti.metrics.aspectj.Metrics;
import lombok.Builder;
import lombok.Getter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Properties;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

import static com.codahale.metrics.MetricRegistry.name;

/**
 * TODO doc.
 */
@Metrics
public class PipelineStreamSource implements MessageSource {
    private static final Logger LOGGER = LoggerFactory.getLogger(PipelineStreamSource.class);
    private final ExecutorService executorService = Executors.newSingleThreadExecutor();


    private final int id = CommsIdGenerator.nextId();
    @Getter
    private final String instanceId;
    private final Properties properties;

    private final TransactionIdGenerator transactionIdGenerator = new SourceIdBasedTransactionIdGenerator(this);
    @Getter
    private final ComponentMetadata sourceMetadata;

    private final NotificationBus notificationBus;

    private final PipelineSource source;
    private final ProcessingContext processingContext;
    private final ObjectMapper objectMapper;
    private final Histogram batchSizeHistogram;

    private final Retryer<PipelineMessage> retryer = RetryerBuilder.<PipelineMessage>newBuilder()
        .retryIfException()
        .retryIfRuntimeException()
        .withWaitStrategy(WaitStrategies.fibonacciWait(30, TimeUnit.SECONDS))
        .build();

    private LinkedBlockingQueue<EventSet> delivered;
    private ConcurrentMap<Long, EventSet> messages;
    private Future<Boolean> generatorFuture;
    private boolean jsonConversion = true;

    @Builder
    public PipelineStreamSource(
        final String instanceId,
        Properties properties,
        NotificationBus notificationBus,
        ComponentMetadata sourceMetadata,
        PipelineSource source,
        ProcessingContext processingContext,
        ObjectMapper objectMapper,
        MetricRegistry registry) {
        this.instanceId = instanceId;
        this.properties = properties;
        this.notificationBus = notificationBus;
        this.sourceMetadata = sourceMetadata;
        this.source = source;
        this.processingContext = processingContext;
        this.objectMapper = objectMapper;
        this.batchSizeHistogram = registry.histogram(name(PipelineStreamSource.class, instanceId, "batchSize"));
    }

    @Override
    public int communicationId() {
        return id;
    }

    @Override
    public boolean sendsNormalMessage() {
        return true;
    }

    public void initialize(Properties globalProperties) throws Exception {
        final Integer count =
                PropertyReader.readInt(this.properties, globalProperties, "computation.eventset.in_flight_count", 5);
        jsonConversion = PropertyReader
                .readBoolean(this.properties, globalProperties, "computation.eventset.is_serialized", true);
        delivered = new LinkedBlockingQueue<>(count);
        messages = Maps.newConcurrentMap();
        source.initialize(instanceId, globalProperties, properties, processingContext, sourceMetadata);
        transactionIdGenerator.seed(seedTransactionId());
        this.notificationBus.source(this);
    }

    protected long seedTransactionId() {
        return 0;
    }

    @Timed(name = "${this.instanceId}.acks")
     public synchronized void ackMessage(EventSet eventSet) {
        if (!messages.containsKey(eventSet.getId())) {
            LOGGER.error("[{}] Event set {} has already been acked. Maybe the topology is weird!!",
                    sourceMetadata.getName(), eventSet.getId());
            return;
        }
        EventSet minMessage = delivered.peek();
        if (null == minMessage) {
            LOGGER.error("[{}] There are no unacked message!! This is impossible!!", sourceMetadata.getName());
            return;
        }
        if (minMessage.getId() != eventSet.getId()) {
            LOGGER.error("[{}] Got an out of bound message. Acceptable: {} Got: {}", sourceMetadata.getName(),
                    minMessage.getId(), eventSet.getId());
            return;
        }
        minMessage = delivered.poll();
        LOGGER.debug("Acked messageset: {} Partition id: {}", minMessage.getId(), minMessage.getPartitionId());
        messages.remove(eventSet.getId());
        source.ack(RawEventBundle.builder()
            .events(minMessage.getEvents())
            .partitionId(minMessage.getPartitionId())
            .transactionId(eventSet.getTransactionId())
            .meta(minMessage.getMeta())
            .build());
    }

    public void start() {
        generatorFuture = executorService.submit(() -> {
            try {
                generateMessage();
            } catch (Exception e) {
                LOGGER.error("Error thrown by source while generating message: ", e);
            }
            return null;
        });
    }

    public void stop() {
        if (null != generatorFuture) {
            generatorFuture.cancel(true);
        }
        executorService.shutdownNow();
    }

    @Timed(name = "${this.instanceId}.batches")
    private RawEventBundle generator() {
        return source.getNewEvents();
    }

    public void generateMessage() throws InterruptedException {
        while (!Thread.currentThread().isInterrupted()) {
            try {
                RawEventBundle eventBundle = generator();
                eventBundle.getEvents().forEach(event -> {
                    if (jsonConversion) {
                        try {
                            if (event.getData() instanceof byte[]) {
                                event.setJsonNode(objectMapper.readTree((byte[]) event.getData()));
                            } else if (event.getData() instanceof String) {
                                event.setJsonNode(objectMapper.readValue((String) event.getData(), ObjectNode.class));
                            } else {
                                event.setJsonNode(objectMapper.valueToTree(event.getData()));
                            }
                        } catch (Throwable t) {
                            LOGGER.error("error generating json payload: ", t);
                        }
                    }
                });
                EventSet eventSet = EventSet.eventFromSourceBuilder()
                    .id(transactionIdGenerator.transactionId())
                    .sourceId(communicationId())
                    .transactionId(eventBundle.getTransactionId())
                    .meta(eventBundle.getMeta())
                    .events(eventBundle.getEvents())
                    .partitionId(eventBundle.getPartitionId())
                    .build();
                batchSizeHistogram.update(eventBundle.getEvents().size());
                messages.put(eventSet.getId(), eventSet);
                delivered.put(eventSet);
                notificationBus.publish(
                    PipelineMessage.userspaceMessageBuilder()
                        .messages(eventSet)
                        .build(),
                    id);

            } catch (Exception e) {
                LOGGER.error("Blocked exception while reading message: ", e);
            }
        }
    }

    public boolean healthcheck() {
        return source.healthcheck();
    }

    //TODO MAYBE WE SHOULD MOVE THE ABOVE TO A THREAD AND HAVE A STOP HERE

}

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

import com.github.rholder.retry.BlockStrategies;
import com.github.rholder.retry.RetryException;
import com.github.rholder.retry.Retryer;
import com.github.rholder.retry.RetryerBuilder;
import com.github.rholder.retry.StopStrategies;
import com.github.rholder.retry.WaitStrategies;
import com.google.common.base.Predicates;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Maps;
import kafka.api.PartitionOffsetRequestInfo;
import kafka.common.TopicAndPartition;
import kafka.javaapi.OffsetResponse;
import kafka.javaapi.TopicMetadata;
import kafka.javaapi.TopicMetadataRequest;
import kafka.javaapi.TopicMetadataResponse;
import kafka.javaapi.consumer.SimpleConsumer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

/**
 * TODO add more.
 */
public final class KafkaMetadataClient {

    private KafkaMetadataClient() {

    }
    private static final Logger LOGGER = LoggerFactory.getLogger(KafkaMetadataClient.class);
    private static final int BUFFER_SIZE = 1_048_576; //1MB

    public static List<HostPort> parseBrokerString(final String brokers) {
        ImmutableList.Builder<HostPort> listBuilder = ImmutableList.builder();
        for (String hostport : brokers.split(",")) {
            final String[] components = hostport.split(":");
            HostPort hostPort = HostPort.builder().host(components[0]).port(Integer.parseInt(components[1])).build();
            listBuilder.add(hostPort);
        }
        return listBuilder.build();
    }

    public static Map<Integer, HostPort> findLeadersForPartitions(final String brokers, final String topic)
            throws BrokerQueryException {

        ImmutableMap.Builder<Integer, HostPort> leadersBuilder = ImmutableMap.builder();
        Retryer<Map<Integer, HostPort>> simpleConsumerRetryer = RetryerBuilder.<Map<Integer, HostPort>>newBuilder()
                .retryIfResult(Predicates.<Map<Integer, HostPort>>isNull()).retryIfException().retryIfRuntimeException()
                .withStopStrategy(StopStrategies.neverStop())
                .withWaitStrategy(WaitStrategies.fixedWait(1, TimeUnit.SECONDS))
                .withBlockStrategy(BlockStrategies.threadSleepStrategy()).build();
        final List<HostPort> brokerList = parseBrokerString(brokers);
        try {
            return simpleConsumerRetryer.call(() -> {
                for (HostPort currentHostPort : brokerList) {
                    SimpleConsumer simpleConsumer = null;
                    try {
                        simpleConsumer = new SimpleConsumer(currentHostPort.getHost(), currentHostPort.getPort(), 1000,
                                BUFFER_SIZE, HostUtils.hostname());
                        TopicMetadataRequest request = new TopicMetadataRequest(Collections.singletonList(topic));
                        TopicMetadataResponse response = simpleConsumer.send(request);
                        for (TopicMetadata metadata : response.topicsMetadata()) {
                            metadata.partitionsMetadata().forEach(partitionMetadata -> leadersBuilder
                                    .put(partitionMetadata.partitionId(),
                                            HostPort.builder().host(partitionMetadata.leader().host())
                                                    .port(partitionMetadata.leader().port()).build()));
                        }
                        return leadersBuilder.build();
                    } finally {
                        if (null != simpleConsumer) {
                            simpleConsumer.close();
                        }
                    }
                }
                return Collections.emptyMap();
            });
        } catch (ExecutionException e) {
            LOGGER.error("Error getting leaders for partitions of " + topic, e);
        } catch (RetryException e) {
            LOGGER.error("Retry error getting leaders for partitions of " + topic, e);
            LOGGER.error("ATTEMPT EXCEPTION: ", e.getLastFailedAttempt().getExceptionCause());
            //System.exit(-1);
            throw new BrokerQueryException("Error finding broker:", e.getLastFailedAttempt().getExceptionCause());
        }
        return Collections.emptyMap();
    }

    public static long startOffset(final SimpleConsumer consumer, final String topic, int partition, String strategy) {
        TopicAndPartition topicAndPartition = new TopicAndPartition(topic, partition);
        Map<TopicAndPartition, PartitionOffsetRequestInfo> requestInfo = Maps.newHashMap();
        if (strategy.equalsIgnoreCase(StartOffsetPickStrategy.EARLIEST.toString())) {
            requestInfo
                    .put(topicAndPartition, new PartitionOffsetRequestInfo(kafka.api.OffsetRequest.EarliestTime(), 1));
        } else {
            requestInfo.put(topicAndPartition, new PartitionOffsetRequestInfo(kafka.api.OffsetRequest.LatestTime(), 1));
        }
        kafka.javaapi.OffsetRequest request =
                new kafka.javaapi.OffsetRequest(requestInfo, kafka.api.OffsetRequest.CurrentVersion(),
                        HostUtils.hostname());
        OffsetResponse response = consumer.getOffsetsBefore(request);

        if (response.hasError()) {
            System.out.println(
                    "Error fetching data Offset Data the Broker. Reason: " + response.errorCode(topic, partition));
            return 0;
        }
        long[] offsets = response.offsets(topic, partition);
        return offsets[0];
    }
}

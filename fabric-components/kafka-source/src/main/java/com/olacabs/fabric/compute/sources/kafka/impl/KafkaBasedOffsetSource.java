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

import com.google.common.collect.Maps;
import kafka.api.ConsumerMetadataRequest;
import kafka.cluster.Broker;
import kafka.common.ErrorMapping;
import kafka.common.OffsetAndMetadata;
import kafka.common.OffsetMetadataAndError;
import kafka.common.TopicAndPartition;
import kafka.javaapi.*;
import kafka.network.BlockingChannel;
import lombok.Builder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static kafka.common.ErrorMapping.OffsetMetadataTooLargeCode;

public class KafkaBasedOffsetSource implements OffsetSource {
    private static final Logger logger = LoggerFactory.getLogger(KafkaBasedOffsetSource.class);

    private final String brokers;
    private final String topology;
    private final String topic;
    private BlockingChannel channel;

    @Builder
    KafkaBasedOffsetSource(String brokers, String topology, String topic) {
        this.brokers = brokers;
        this.topology = topology;
        this.topic = topic;
        reconnect();
    }

    @Override
    public void saveOffset(String topic, int partition, long offset) throws Exception {
        while (true) {
            long now = System.currentTimeMillis();
            Map<TopicAndPartition, OffsetAndMetadata> offsets = Maps.newLinkedHashMap();
            offsets.put(new TopicAndPartition(topic, partition), new OffsetAndMetadata(100L, "", now));
            OffsetCommitRequest commitRequest = new OffsetCommitRequest(
                groupId(),
                offsets,
                0, //TODO::CORRELATION ID
                HostUtils.hostname(),
                (short) 1 /* version */); // version 1 and above commit to Kafka, version 0 commits to ZooKeeper
            try {
                channel.send(commitRequest.underlying());
                OffsetCommitResponse commitResponse = OffsetCommitResponse.readFrom(channel.receive().buffer());
                if (commitResponse.hasError()) {
                    for (Object partitionErrorCodeRaw : commitResponse.errors().values()) {
                        short partitionErrorCode = (Short) partitionErrorCodeRaw;
                        if (partitionErrorCode == OffsetMetadataTooLargeCode()) {
                            // You must reduce the size of the metadata if you wish to retry
                            //TODO::SAVE META MAP
                        } else if (partitionErrorCode == ErrorMapping.NotCoordinatorForConsumerCode() || partitionErrorCode == ErrorMapping.ConsumerCoordinatorNotAvailableCode()) {
                            channel.disconnect();
                            reconnect();
                        } else {
                            //TODO log and retry the commit
                        }
                    }
                } else {
                    return;
                }
            } catch (Exception ioe) {
                channel.disconnect();
                reconnect();
            }
        }
    }

    @Override
    public long startOffset(String topic, int partition) throws Exception {
        TopicAndPartition testPartition = new TopicAndPartition(topic, partition);
        while (true) {
            List<TopicAndPartition> partitions = new ArrayList<>();
            partitions.add(testPartition);
            OffsetFetchRequest fetchRequest = new OffsetFetchRequest(
                groupId(),
                partitions,
                (short) 1 /* version */, // version 1 and above fetch from Kafka, version 0 fetches from ZooKeeper
                0,
                HostUtils.hostname());
            try {
                channel.send(fetchRequest.underlying());
                OffsetFetchResponse fetchResponse = OffsetFetchResponse.readFrom(channel.receive().buffer());
                OffsetMetadataAndError result = fetchResponse.offsets().get(testPartition);
                if (null == result) {
                    return 0;
                }
                short offsetFetchErrorCode = result.error();
                if (offsetFetchErrorCode == ErrorMapping.NotCoordinatorForConsumerCode()) {
                    channel.disconnect();
                    reconnect();
                    // Go to step 1 and retry the offset fetch
                } /*else if (offsetFetchErrorCode == ErrorMapping.OffsetsLoadInProgress()) {
                // retry the offset fetch (after backoff)
            } */ else {
                    String retrievedMetadata = result.metadata();
                    return result.offset();
                }
            } catch (Exception e) {
                channel.disconnect();
                logger.error("Error fetching offset: ", e);
                // Go to step 1 and then retry offset fetch after backoff
                reconnect();
            }
        }
    }

    private void reconnect() {
        final String MY_GROUP = groupId();
        final String MY_CLIENTID = HostUtils.hostname();
        while (true) {
            try {
                channel = new BlockingChannel("localhost", 9092,
                    BlockingChannel.UseDefaultBufferSize(),
                    BlockingChannel.UseDefaultBufferSize(),
                    5000 /* read timeout in millis */);
                channel.connect();
                int correlationId = 0;

                channel.send(new ConsumerMetadataRequest(MY_GROUP, ConsumerMetadataRequest.CurrentVersion(), correlationId++, MY_CLIENTID));
                ConsumerMetadataResponse metadataResponse = ConsumerMetadataResponse.readFrom(channel.receive().buffer());

                if (metadataResponse.errorCode() == ErrorMapping.NoError()) {
                    Broker offsetManager = metadataResponse.coordinator();
                    // if the coordinator is different, from the above channel's host then reconnect
                    channel.disconnect();
                    channel = new BlockingChannel(offsetManager.host(), offsetManager.port(),
                        BlockingChannel.UseDefaultBufferSize(),
                        BlockingChannel.UseDefaultBufferSize(),
                        5000 /* read timeout in millis */);
                    channel.connect();
                    break;
                } else {
                    // retry (after backoff)
                }
            } catch (Exception e) {
                // retry the query (after backoff)
                logger.error("Error reconnecting: ", e);
            }
        }
    }

    private String groupId() {
        return String.format("fabric.%s.%s", topology, topic);
    }
}

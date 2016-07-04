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
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.olacabs.fabric.model.event.RawEventBundle;
import lombok.Builder;
import lombok.Getter;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.recipes.leader.LeaderSelector;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Map;
import java.util.concurrent.LinkedBlockingQueue;

/**
 * Created by santanu.s on 07/10/15.
 */

public class Balancer {
    private static final Logger logger = LoggerFactory.getLogger(Balancer.class);

    private final String topologyName;
    private final String brokers;
    private final String topic;
    private final CuratorFramework curatorFramework;
    private final ObjectMapper objectMapper;
    private int bufferSize;
    private Map<Integer, KafkaMessageReader> readers = Maps.newHashMap();
    private List<LeaderSelector> leaderSelectors = Lists.newArrayList();
    private String startOffsetPickStrategy;

    @Getter
    private LinkedBlockingQueue<RawEventBundle> events;

    @Builder
    public Balancer(String instanceId, String topologyName, String brokers, String topic,
                    CuratorFramework curatorFramework, ObjectMapper objectMapper, int bufferSize, String startOffsetPickStrategy) {
        this.topologyName = topologyName;
        this.brokers = brokers;
        this.topic = topic;
        this.curatorFramework = curatorFramework;
        this.objectMapper = objectMapper;
        this.bufferSize = bufferSize;
        this.startOffsetPickStrategy = startOffsetPickStrategy;
    }

    public void start() throws Exception {
        Map<Integer, HostPort> leaders = KafkaMetadataClient.findLeadersForPartitions(brokers, topic);
        if (leaders.isEmpty()) {
            throw new Exception("Could not find any leaders for any of the partitions in topic: " + topic);
        }
        logger.info("Number of leaders: {}", leaders.size());
        events = new LinkedBlockingQueue<>(leaders.size());
        KafkaConsumerBuilder consumerBuilder = KafkaConsumerBuilder.builder()
            .brokers(brokers)
            .instanceId(HostUtils.hostname())
            .build();
        OffsetSource offsetSource = ZookeeperOffsetSource.builder()
            .curatorFramework(curatorFramework)
            .objectMapper(objectMapper)
            .topologyName(topologyName)
            .topicName(topic)
            .build();
        leaders.entrySet().forEach(entry -> {
            readers.put(entry.getKey(),
                KafkaMessageReader.builder()
                    .bufferSize(bufferSize)
                    .consumerBuilder(consumerBuilder)
                    .events(events)
                    .offsetSource(offsetSource)
                    .partition(entry.getKey())
                    .startOffsetPickStrategy(startOffsetPickStrategy)
                    .topic(topic)
                    .transactionManager(
                        TransactionManager.builder()
                            .topic(topic)
                            .topologyName(topologyName)
                            .mapper(objectMapper)
                            .curator(curatorFramework)
                            .partition(entry.getKey())
                            .build())
                    .build());
            logger.info("Created reader object for {}", topic);
        });
        readers.values().forEach(kafkaMessageReader -> {
            try {
                kafkaMessageReader.start();
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        });
        KafkaReaderLeaderElector leaderElector = new KafkaReaderLeaderElector(topologyName, topic, readers, curatorFramework, objectMapper);
        leaderElector.start();
    }

    public void stop() {
        try {
            readers.values().forEach(KafkaMessageReader::stop);
        } catch (Exception e) {
            logger.error("Error stopping reader: ", e);
        }
        try {
            leaderSelectors.forEach(LeaderSelector::close);
        } catch (Exception e) {
            logger.error("Error closing selector: ", e);
        }
    }

    public void ack(RawEventBundle rawEventBundle) {
        try {
            logger.info("To ack for: {}", rawEventBundle.getPartitionId());
            readers.get(rawEventBundle.getPartitionId()).ackMessage(rawEventBundle);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}

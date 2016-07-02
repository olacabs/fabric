package com.olacabs.fabric.compute.sources.kafka.impl;

import kafka.javaapi.consumer.SimpleConsumer;
import lombok.Builder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;

@Builder
public class KafkaConsumerBuilder {
    private static final Logger logger = LoggerFactory.getLogger(KafkaConsumerBuilder.class);
    private static final int BUFFER_SIZE = 2 ^ 20;

    private final String brokers;
    private final String instanceId;

    public Map<Integer, SimpleConsumer> consumersForTopic(final String topic) throws Exception {
        Map<Integer, HostPort> leaders = KafkaMetadataClient.findLeadersForPartitions(brokers, topic);
        if (leaders.isEmpty()) {
            throw new Exception("Could not find any leaders for any of the partitions in topic: " + topic);
        }
        Map<Integer, SimpleConsumer> leaderConsumers = new HashMap<Integer, SimpleConsumer>();
        for (Map.Entry<Integer, HostPort> leader : leaders.entrySet()) {
            HostPort leaderHost = leader.getValue();
            SimpleConsumer consumer = new SimpleConsumer(leaderHost.getHost(),
                leaderHost.getPort(), 1000, BUFFER_SIZE, HostUtils.hostname());
            leaderConsumers.put(leader.getKey(), consumer);
            logger.info("Created consumer for {}:{}", topic, leader.getKey());
        }
        return leaderConsumers;
    }

    public SimpleConsumer consumersForTopicPartition(final String topic, int partition) throws Throwable {
        Map<Integer, HostPort> leaders = KafkaMetadataClient.findLeadersForPartitions(brokers, topic);
        if (leaders.isEmpty()) {
            throw new Exception("Could not find any leaders for any of the partitions in topic: " + topic);
        }
        if (leaders.containsKey(partition)) {
            HostPort leaderHost = leaders.get(partition);
            return new SimpleConsumer(leaderHost.getHost(),
                leaderHost.getPort(), 100000, BUFFER_SIZE, HostUtils.hostname());
        }
        throw new Exception(String.format("Could not find any leaders for [%s:%d]", topic, partition));
    }


}

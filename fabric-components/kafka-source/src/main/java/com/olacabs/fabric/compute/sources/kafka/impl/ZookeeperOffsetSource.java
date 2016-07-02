package com.olacabs.fabric.compute.sources.kafka.impl;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.Sets;
import lombok.Builder;
import org.apache.curator.framework.CuratorFramework;
import org.apache.zookeeper.KeeperException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collections;
import java.util.Map;
import java.util.Set;

/**
 * Zookeeper based offset manager
 */
public class ZookeeperOffsetSource implements OffsetSource {
    private static final Logger logger = LoggerFactory.getLogger(ZookeeperOffsetSource.class);

    private final String topologyName;
    private final String topicName;
    private final CuratorFramework curatorFramework;
    private final ObjectMapper objectMapper;

    private final Set<String> ensuredPaths = Sets.newConcurrentHashSet();

    @Builder
    public ZookeeperOffsetSource(String topologyName, String topicName, CuratorFramework curatorFramework, ObjectMapper objectMapper) {
        this.topologyName = topologyName;
        this.topicName = topicName;
        this.curatorFramework = curatorFramework;
        this.objectMapper = objectMapper;
        final String path = path(topologyName, topicName);
        try {
            if (null != curatorFramework.checkExists().forPath(path)) {
                curatorFramework.create().creatingParentContainersIfNeeded().forPath(path);
            }
        } catch (KeeperException.NodeExistsException e) {
            logger.info("{} already exists.", path);
        } catch (Exception e) {
            logger.warn("Error creating path on ZK: {}", path, e);
        }
    }

    private static String path(String topologyName, String consumerName) {
        return String.format("/%s/%s/offsets", topologyName, consumerName);
    }

    private static String partitionPath(String topologyName, String consumerName, int partition) {
        return String.format("/%s/%s/offsets/%d", topologyName, consumerName, partition);
    }

    @Override
    public void saveOffset(String topic, int partition, long offset) throws Exception {
        //TODO::BATCH THIS
        final String path = partitionPath(topologyName, topicName, partition);
        if (!ensuredPaths.contains(path)) {
            if (null == curatorFramework.checkExists().forPath(path)) {
                curatorFramework.create().creatingParentContainersIfNeeded().forPath(path);
            }
        }
        ensuredPaths.add(path);
        curatorFramework.setData().forPath(path, objectMapper.writeValueAsBytes(Collections.singletonMap("offset", offset)));
    }

    @Override
    public long startOffset(String topic, int partition) throws Exception {
        final String path = partitionPath(topologyName, topicName, partition);
        if (!ensuredPaths.contains(path)) {
            if (null == curatorFramework.checkExists().forPath(path)) {
                return -1;
            }
            ensuredPaths.add(path);
        }
        byte[] data = curatorFramework.getData().forPath(path);
        if (null == data || 0 == data.length) {
            return -1;
        }
        Map<String, Long> offset = objectMapper.readValue(data, new TypeReference<Map<String, Long>>() {
        });
        return offset.get("offset");
    }
}

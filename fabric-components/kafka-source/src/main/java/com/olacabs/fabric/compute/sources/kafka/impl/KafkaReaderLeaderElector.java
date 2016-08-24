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
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.recipes.leader.LeaderSelector;
import org.apache.curator.framework.recipes.leader.LeaderSelectorListener;
import org.apache.curator.framework.state.ConnectionState;
import org.apache.zookeeper.CreateMode;
import org.apache.zookeeper.KeeperException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

/**
 * TODO java doc.
 */
public class KafkaReaderLeaderElector implements LeaderSelectorListener {
    private static final Logger LOGGER = LoggerFactory.getLogger(KafkaReaderLeaderElector.class);

    private final String topology;
    private final String topic;
    private final Map<Integer, KafkaMessageReader> readers;
    private final CuratorFramework curatorFramework;
    private final ObjectMapper mapper;

    private final String readerId;
    private final AtomicBoolean stop = new AtomicBoolean();
    private LeaderSelector leaderSelector;
    private ReentrantLock lock = new ReentrantLock();
    private Condition condition = lock.newCondition();
    private ExecutorService executorService;
    private ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();
    private Map<Integer, AtomicBoolean> isRunning = Maps.newHashMap();
    private Set<String> knownMembers = Sets.newHashSet();

    public KafkaReaderLeaderElector(String topology, String topic, Map<Integer, KafkaMessageReader> readers,
            CuratorFramework curatorFramework, ObjectMapper mapper) {
        this.topology = topology;
        this.topic = topic;
        this.readers = readers;
        this.curatorFramework = curatorFramework;
        this.mapper = mapper;

        this.readerId = UUID.randomUUID().toString();
        this.executorService = Executors.newCachedThreadPool(); //= Executors.newFixedThreadPool(readers.size());
    }

    public void start() throws Exception {
        readers.keySet().forEach(partiton -> {
            isRunning.put(partiton, new AtomicBoolean(false));
        });
        scheduler.scheduleWithFixedDelay(() -> {
            LOGGER.debug("Checking zookeeper");
            try {
                readers.keySet().forEach(partiton -> {
                    try {
                        final String communicatorPath = communicatorPath(partiton);
                        byte[] data = curatorFramework.getData().forPath(communicatorPath);
                        final String selectedReader = new String(data);
                        if (readerId.equals(selectedReader)) {
                            if (isRunning.get(partiton).compareAndSet(false, true)) {
                                LOGGER.info("[{}:{}:{}] Got start", topology, topic, partiton);
                                executorService.submit(readers.get(partiton));
                            } else {
                                LOGGER.debug("[{}:{}:{}] Nothing changed .. already running...", topology, topic,
                                        partiton);
                            }
                        } else {
                            if (isRunning.get(partiton).compareAndSet(true, false)) {
                                LOGGER.info("[{}:{}:{}] Got stop", topology, topic, partiton);
                                readers.get(partiton).stop();
                            } else {
                                LOGGER.debug("[{}:{}:{}] Nothing changed .. already stopped...", topology, topic,
                                        partiton);
                            }
                        }
                    } catch (KeeperException.NoNodeException e) {
                        LOGGER.info("[{}:{}:{}] Communicator not yet initialized", topology, topic, partiton);
                    } catch (Throwable e) {
                        LOGGER.error("[{}:{}:{}] Error reading ZK node for reader id", topology, topic, partiton);
                        throw new RuntimeException(e);
                    }
                });
                peerCountChange();
            } catch (Throwable t) {
                LOGGER.info("Error detecting membership changes.", t);
            }
        }, 0, 10, TimeUnit.SECONDS);
        LOGGER.info("[{}:{}:{}] Creating communicators", topology, topic, memberPathPrefix());
        LOGGER.info("[{}:{}:{}] Watching reader path: {}", topology, topic, memberPathPrefix());
        final String leaderPath = String.format("/%s/%s/loadbalancer-leader", topology, topic);
        this.leaderSelector = new LeaderSelector(curatorFramework, leaderPath, this);
        LOGGER.info("Starting leader selector at: " + leaderPath);
        curatorFramework.create().creatingParentContainersIfNeeded().withMode(CreateMode.EPHEMERAL)
                .forPath(memberPath());
        LOGGER.info("Member path created at: " + memberPath());
        leaderSelector.start();
    }

    public void stop() {
        lock.lock();
        try {
            stop.set(true);
            condition.signal();
        } finally {
            lock.unlock();
        }
    }

    @Override
    public void takeLeadership(CuratorFramework client) throws Exception {
        LOGGER.info("[{}:{}] This coordinator became the leader.", topology, topic);
        peerCountChange(true);
        while (true) {
            lock.lock();
            try {
                while (!stop.get()) {
                    condition.await();
                }
                if (stop.get()) {
                    LOGGER.info("[{}:{}] Exiting coordinator.", topology, topic);
                    return;
                }
            } finally {
                lock.unlock();
            }
        }
    }

    @Override
    public void stateChanged(CuratorFramework client, ConnectionState newState) {

    }

    private void peerCountChange() {
        peerCountChange(false);
    }

    private void peerCountChange(boolean force) {
        final String memberPath = memberPathPrefix();
        List<String> members = null;
        try {
            members = curatorFramework.getChildren().forPath(memberPath);
            LOGGER.debug("Members: " + members);
            if (Sets.symmetricDifference(knownMembers, Sets.newHashSet(members)).isEmpty() && !force) {
                LOGGER.debug("No membership changes detected");
                return;
            } //.intersection(knownMembers, Sets.newHashSet(members))
        } catch (Exception e) {
            if (e instanceof KeeperException.NodeExistsException) {
                LOGGER.info("Looks like this topology/topic combination is being used for the first time");
            } else {
                LOGGER.error("Error checking for node on ZK: ", e);
            }
        }
        if (null == members) {
            LOGGER.error("No members found .. how did i come here? ZK issue?");
            return;
        }
        if (null == leaderSelector || !leaderSelector.hasLeadership()) {
            LOGGER.debug("I'm not the leader coordinator");
            return;
        }
        knownMembers = Sets.newHashSet(members);
        final List<String> finalMembers = members;
        AtomicInteger counter = new AtomicInteger(0);
        readers.keySet().forEach(partition -> {
            String selectedReader = finalMembers.get(counter.getAndIncrement() % finalMembers.size());
            LOGGER.info("[{}:{}:{}] Selected reader: {}", topology, topic, partition, selectedReader);
            final String communicatorPath = communicatorPath(partition);
            try {
                if (null == curatorFramework.checkExists().creatingParentContainersIfNeeded()
                        .forPath(communicatorPath)) {
                    curatorFramework.create().creatingParentContainersIfNeeded().forPath(communicatorPath);
                    LOGGER.info("[{}:{}:{}] Created communicator", topology, topic, partition);
                }
                curatorFramework.setData().forPath(communicatorPath, selectedReader.getBytes());
                LOGGER.error("Set reader at {} to {}", communicatorPath, selectedReader);
            } catch (Exception e) {
                LOGGER.error("Error setting reader value at {} to {}", communicatorPath, selectedReader, e);
            }
        });
    }

    private String communicatorPath(int partition) {
        return String.format("/%s/%s/readers/%d", topology, topic, partition);
    }

    private String memberPath() {
        return String.format("/%s/%s/loadbalancer/%s", topology, topic, readerId);
    }

    private String memberPathPrefix() {
        return String.format("/%s/%s/loadbalancer", topology, topic);
    }


}

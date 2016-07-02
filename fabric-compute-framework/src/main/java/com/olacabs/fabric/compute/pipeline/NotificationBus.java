package com.olacabs.fabric.compute.pipeline;

import com.codahale.metrics.*;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.olacabs.fabric.compute.comms.ChannelFactory;
import com.olacabs.fabric.compute.comms.CommsChannel;
import com.olacabs.fabric.compute.processor.ProcessingException;
import com.olacabs.fabric.compute.source.PipelineStreamSource;
import com.olacabs.fabric.compute.tracking.SimpleBitSet;
import com.olacabs.fabric.model.event.EventSet;
import lombok.Builder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;
import java.util.Properties;
import java.util.Set;

/**
 * Created by santanu.s on 11/09/15.
 */
public class NotificationBus {
    private static final Logger logger = LoggerFactory.getLogger(NotificationBus.class);

    private Properties properties;

    private static class Connection {
        private final Set<Integer> pipelineStages;

        public Connection() {
            pipelineStages = Sets.newHashSet();
        }

        Connection addConnection(int pipelineStage) {
            pipelineStages.add(pipelineStage);
            return this;
        }
    }

    @Builder
    public static class Communicator {
        private MessageSource pipelineStage;
        private CommsChannel<PipelineMessage> commsChannel;
    }

    private Map<Integer, Connection> connections = Maps.newHashMap();
    private Map<Integer, Communicator> comms = Maps.newHashMap();
    private Map<Integer, PipelineStreamSource> sources = Maps.newHashMap();
    private final Map<Long, SimpleBitSet> tracker = Maps.newConcurrentMap();

    public NotificationBus(final Properties properties) {
        this.properties = properties;
        logger.info("Notification bus created...");
    }

    public NotificationBus source(PipelineStreamSource streamSource) {
        sources.put(streamSource.communicationId(), streamSource);
        return this;
    }

    public NotificationBus connect(MessageSource to, PipelineStage ... pipelineStages) {
        if(!connections.containsKey(to.communicationId())) {
            connections.put(to.communicationId(), new Connection());
        }
        for(PipelineStage pipelineStage : pipelineStages) {
            connections.get(to.communicationId()).addConnection(pipelineStage.communicationId());
            if (!comms.containsKey(pipelineStage.communicationId())) {
                comms.put(pipelineStage.communicationId(),
                        Communicator.builder()
                                .commsChannel(ChannelFactory.create(properties, pipelineStage.name(), false, pipelineStage))
                                .pipelineStage(pipelineStage)
                                .build());
            }
        }
        return this;
    }

    synchronized public void publish(PipelineMessage message, int from) {
        publish(message, from, true);
    }

    synchronized public void publish(PipelineMessage message, int from, boolean forward) {
        switch (message.getMessageType()) {
            case TIMER: {
                // It is a timer message, ACKing is disabled
                comms.get(from).commsChannel.publish(message);
                break;
            }
            case USERSPACE: {
                // It is a userspace message, ACKing is enabled
                PipelineMessage actionableMessage = message;
                ImmutableSet.Builder<EventSet> ackCandidatesBuilder = ImmutableSet.builder();
                if(!message.getMessages().isAggregate()) {
                    // Find out the oldest ancestor of this event set
                    while(true) {
                        if(null == actionableMessage.getParent()) {
                            break;
                        }
                        actionableMessage = actionableMessage.getParent();
                    }

                    if (!tracker.containsKey(actionableMessage.getMessages().getId())) {
                        // Set up the tracker for this event set since it is sent from a source for the first time
                        // Add this event set to the tracker with an empty bitset
                        tracker.put(actionableMessage.getMessages().getId(), new SimpleBitSet(64));
                    }
                    SimpleBitSet msgBitSet = tracker.get(actionableMessage.getMessages().getId());
                    try {
                        // If the event set is being sent forward and the sender sends normal message, set bits
                        // for each of the receivers based on the auto incremented id assigned to them
                        if (forward
                                && connections.containsKey(from)
                                && ((comms.containsKey(from) && comms.get(from).pipelineStage.sendsNormalMessage())
                                || !comms.containsKey(from))) {
                            connections.get(from).pipelineStages.stream().forEach(msgBitSet::set);
                        }
                    } catch (Exception e) {
                        logger.error("Error setting tracking bits for generator: {}", from, e);
                    }

                    // unset the bit corresponding to the sender
                    msgBitSet.unset(from);
                    // if all the bits are unset and if this event set is generated by a source, mark the
                    // event set as a candidate for ACKing
                    if (!msgBitSet.hasSetBits() && actionableMessage.getMessages().isSourceGenerated()) {
                        ackCandidatesBuilder.add(actionableMessage.getMessages());
                    }
                }
                try {
                    // publish the message to each of the receivers
                    if(forward && connections.containsKey(from)) {
                        connections.get(from)
                                .pipelineStages
                                .stream()
                                .forEach(pipeLineStage -> comms.get(pipeLineStage).commsChannel.publish(message));
                    }
                } catch (Throwable t) {
                    logger.error("Error sending event to children for {}", from, t);
                }
                // event sets which are eligible for ACKing
                ImmutableSet<EventSet> ackSet = ackCandidatesBuilder.build();

                // ACK all the event sets
                ackSet.stream()
                            .forEach(eventSet -> sources.get(eventSet.getSourceId()).ackMessage(eventSet));
                // No event set to ACK, hence we can remove the tracker entry for this event set
                if (!ackSet.isEmpty()) {
                    tracker.remove(actionableMessage.getMessages().getId());
                }

                break;
            }
        }

    }

    public void start() {
        connections = ImmutableMap.copyOf(connections);
        comms.values().forEach(communicator -> communicator.commsChannel.start());
    }

    public void stop() {
        comms.values().forEach(communicator -> communicator.commsChannel.stop());
    }

}

package com.olacabs.fabric.compute.comms;

import com.olacabs.fabric.compute.util.ComponentPropertyReader;

import java.util.Properties;

/**
 * Created by santanu.s on 10/09/15.
 */
public class ChannelFactory {
    public static <EventType> CommsChannel<EventType> create(final Properties properties,
                                                             final String name,
                                                             final boolean isSingleProducer,
                                                             final CommsMessageHandler<EventType> handler) {
        String channelType = (String) properties.getOrDefault("computation.channel.channel_type",
            ChannelType.BLOCKING_QUEUE.toString());
        channelType = channelType.trim();

        if (channelType.equalsIgnoreCase(ChannelType.BLOCKING_QUEUE.toString())) {
            return new BlockingQueueCommsChannel<>(name, isSingleProducer, handler);
        } else {
            int bufferSize = Integer.valueOf((String) properties.getOrDefault("computation.disruptor.buffer_size", "64"));
            String waitStrategy = (String) properties.getOrDefault("computation.disruptor.wait_strategy", DisruptorWaitStrategy.BLOCK.toString());
            waitStrategy = waitStrategy.trim();
            waitStrategy = waitStrategy.toLowerCase();
            return new DisruptorCommsChannel<>(name, isSingleProducer, waitStrategy, bufferSize, handler);
        }
    }
}

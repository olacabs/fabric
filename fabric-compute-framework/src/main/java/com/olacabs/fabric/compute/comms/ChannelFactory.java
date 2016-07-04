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

package com.olacabs.fabric.compute.comms;

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

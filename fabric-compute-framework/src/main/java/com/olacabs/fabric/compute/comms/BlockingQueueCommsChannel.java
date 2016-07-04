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

import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.LinkedBlockingQueue;

/**
 * Created by santanu.s on 10/09/15.
 */
@Slf4j
public class BlockingQueueCommsChannel<EventType> implements CommsChannel<EventType> {
    private final String name;
    private final boolean isSingleProducer;
    private final CommsMessageHandler<EventType> handler;
    private ExecutorService executorService = Executors.newSingleThreadExecutor();
    private LinkedBlockingQueue<CommsFrameworkMessage<EventType>> queue;
    private Future jobFuture;

    BlockingQueueCommsChannel(String name, boolean isSingleProducer, CommsMessageHandler<EventType> handler) {
        this.isSingleProducer = isSingleProducer;
        this.handler = handler;
        this.name = name;
    }

    @Override
    public String name() {
        return name;
    }

    @Override
    public void publish(EventType sourceEvent) {
        try {
            queue.put(
                (CommsFrameworkMessage<EventType>) CommsFrameworkMessage.<EventType>builder()
                    .id(0)
                    .payload(sourceEvent)
                    .source(name)
                    .build());
        } catch (InterruptedException e) {
            log.error("Comms channel stopped");
        }
    }

    @Override
    public void start() {
        queue = new LinkedBlockingQueue<>(8);
        jobFuture = executorService.submit(() -> {
            while (true) {
                CommsFrameworkMessage<EventType> message = queue.take();
                try {
                    handler.handlePipelineMessage(message.getPayload());
                } catch (Throwable t) {
                    throw new RuntimeException("Error sending message to processor: ", t);
                }
            }
        });
    }

    @Override
    public void stop() {
        if (null != jobFuture) {
            jobFuture.cancel(true);
        }
    }
}

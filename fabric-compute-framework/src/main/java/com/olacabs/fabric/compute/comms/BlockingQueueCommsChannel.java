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
 *
 * TODO add javadoc.
 */
@Slf4j
public class BlockingQueueCommsChannel<E> implements CommsChannel<E> {
    private final String name;
    private final boolean isSingleProducer;
    private final CommsMessageHandler<E> handler;
    private ExecutorService executorService = Executors.newSingleThreadExecutor();
    private LinkedBlockingQueue<CommsFrameworkMessage<E>> queue;
    private Future jobFuture;

    BlockingQueueCommsChannel(String name, boolean isSingleProducer, CommsMessageHandler<E> handler) {
        this.isSingleProducer = isSingleProducer;
        this.handler = handler;
        this.name = name;
    }

    @Override
    public String name() {
        return name;
    }

    @Override
    public void publish(E sourceEvent) {
        try {
            queue.put(
                (CommsFrameworkMessage<E>) CommsFrameworkMessage.<E>builder()
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
                CommsFrameworkMessage<E> message = queue.take();
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

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
        if(null != jobFuture) {
            jobFuture.cancel(true);
        }
    }
}

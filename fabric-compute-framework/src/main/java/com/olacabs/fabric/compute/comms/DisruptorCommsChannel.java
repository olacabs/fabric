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

import com.lmax.disruptor.*;
import com.lmax.disruptor.dsl.Disruptor;
import com.lmax.disruptor.dsl.ProducerType;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

@Slf4j
@Setter
@Getter
public class DisruptorCommsChannel<EventType> implements CommsChannel<EventType> {
    private static final Logger logger = LoggerFactory.getLogger(BlockingQueueCommsChannel.class);

    private final String name;
    private final boolean isSingleProducer;
    private final CommsMessageHandler<EventType> handler;
    private ExecutorService executorService = Executors.newSingleThreadExecutor();
    private int bufferSize;
    private Disruptor<CommsFrameworkMessage<EventType>> disruptor;

    public DisruptorCommsChannel(final String name,
                                 final boolean isSingleProducer,
                                 final String waitStrategy,
                                 final int bufferSize,
                                 final CommsMessageHandler<EventType> handler) {
        this.isSingleProducer = isSingleProducer;
        this.handler = handler;
        this.name = name;
        if ((bufferSize & (bufferSize - 1)) == 0) {
            this.bufferSize = bufferSize;
        } else {
            throw new IllegalArgumentException("Disruptor buffer size must always be a power of 2");
        }
        WaitStrategy waitStrategy1;
        switch (waitStrategy) {
            case "block":
                waitStrategy1 = new BlockingWaitStrategy();
                break;
            case "lite":
                waitStrategy1 = new LiteBlockingWaitStrategy();
                break;
            case "timeout":
                waitStrategy1 = new TimeoutBlockingWaitStrategy(10, TimeUnit.MILLISECONDS);
                break;
            case "sleep":
                waitStrategy1 = new SleepingWaitStrategy();
                break;
            case "yield":
                waitStrategy1 = new YieldingWaitStrategy();
                break;
            case "busy":
                waitStrategy1 = new BusySpinWaitStrategy();
                break;
            default:
                waitStrategy1 = new SleepingWaitStrategy();
        }
        if (isSingleProducer) {
            this.disruptor = new Disruptor<>(CommsFrameworkMessage<EventType>::new,
                bufferSize,
                executorService,
                ProducerType.SINGLE,
                waitStrategy1);
        } else {
            this.disruptor = new Disruptor<>(CommsFrameworkMessage<EventType>::new,
                bufferSize,
                executorService,
                ProducerType.MULTI,
                waitStrategy1);
        }
    }

    @Override
    public String name() {
        return name;
    }

    @Override
    public void publish(final EventType sourceEvent) {
        try {
            RingBuffer<CommsFrameworkMessage<EventType>> ringBuffer = disruptor.getRingBuffer();
            ringBuffer.publishEvent((event, sequence) -> {
                event.setId(sequence);
                event.setSource(name);
                event.setPayload(sourceEvent);
            });
        } catch (Exception e) {
            log.error("Comms channel stopped");
        }
    }

    @Override
    public void start() {
        disruptor.handleEventsWith((event, sequence, endOfBatch) -> {
            try {
                handler.handlePipelineMessage(event.getPayload());
            } catch (Throwable t) {
                throw new RuntimeException("Error sending message to processor: ", t);
            }
        });
        disruptor.start();
    }

    @Override
    public void stop() {
        disruptor.halt();
    }
}

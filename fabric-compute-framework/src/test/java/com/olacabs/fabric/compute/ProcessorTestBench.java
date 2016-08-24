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

package com.olacabs.fabric.compute;

import com.codahale.metrics.ConsoleReporter;
import com.codahale.metrics.CsvReporter;
import com.codahale.metrics.MetricFilter;
import com.codahale.metrics.MetricRegistry;
import com.codahale.metrics.ScheduledReporter;
import com.codahale.metrics.SharedMetricRegistries;
import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import com.google.common.collect.ImmutableList;
import com.olacabs.fabric.compute.processor.ProcessingException;
import com.olacabs.fabric.compute.processor.ProcessorBase;
import com.olacabs.fabric.compute.processor.ScheduledProcessor;
import com.olacabs.fabric.compute.processor.StreamingProcessor;
import com.olacabs.fabric.model.event.EventSet;
import lombok.extern.slf4j.Slf4j;

import java.io.File;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;

/**
 * doc.
 */
@Slf4j
public class ProcessorTestBench {
    private ScheduledReporter reporter;
    private MetricRegistry metricRegistry;

    /**
     * A constructor for getting an instance of {@code ProcessorTestBench}.
     *
     * @param metricsEnabled - if true, then metrics writes metrics to the console
     */
    public ProcessorTestBench(final boolean metricsEnabled) {
        if (metricsEnabled) {
            metricRegistry = SharedMetricRegistries.getOrCreate("metrics-registry");
            metricRegistry.timer("consume-timer");
            reporter = ConsoleReporter.forRegistry(metricRegistry)
                .convertDurationsTo(TimeUnit.MILLISECONDS)
                .convertRatesTo(TimeUnit.SECONDS)
                .filter(MetricFilter.ALL)
                .build();
        }
    }

    /**
     * A constructor for getting an instance of {@code ProcessorTestBench} that writes metrics to a csv file.
     *
     * @param dirPath The relative or absolute path of the directory to place csv files for each metric
     */
    public ProcessorTestBench(final String dirPath) {
        Preconditions.checkArgument(!Strings.isNullOrEmpty(dirPath),
            "Provide a non-null and non-empty filePath");
        File dir = new File(dirPath);
        Preconditions.checkArgument(dir.exists() || dir.mkdirs(),
                "Provide a directory path which either exists or can be created");
        metricRegistry = SharedMetricRegistries.getOrCreate("metrics-registry");
        metricRegistry.timer("consume-timer");
        reporter = CsvReporter.forRegistry(metricRegistry)
            .convertRatesTo(TimeUnit.SECONDS)
            .convertDurationsTo(TimeUnit.MILLISECONDS)
            .filter(MetricFilter.ALL)
            .build(dir);
    }

    private void startReporter() {
        if (reporter != null) {
            reporter.start(1, TimeUnit.SECONDS);
            log.info("Metrics reporter started");
        }
    }

    private void stopReporter() {
        if (reporter != null) {
            reporter.stop();
            log.info("Metrics reporter stopped");
        }
    }

    /**
     * A method for functional testing of a time-driven processor.
     *
     * @param processor      - an instance of {@code ScheduledProcessor} that has been initialized already
     * @param pulseDelay     - fixed delay at which pulses (one pulse corresponds to one invocation of
     *                       timetriggerhandler on the processor) are delivered
     * @param numPulses      - number of pulses
     * @param incomingEvents - a list of {@code EventSet}
     * @return a list of {@code EventSet} returned by the processor after processing the incoming events
     * @throws Exception
     */
    public List<EventSet> runScheduledProcessor(final ScheduledProcessor processor, long pulseDelay, long numPulses,
            List<EventSet> incomingEvents) throws Exception {
        Preconditions.checkNotNull(processor, "Processor can't be null!!");
        Preconditions
                .checkNotNull(incomingEvents, "Please provide events to be sent to the processor.consume() method");
        Preconditions.checkArgument(!incomingEvents.isEmpty(),
                "Please provide events to be sent to the processor.consume() method");
        Preconditions.checkArgument(pulseDelay > 0, "Please provide a positive pulse delay");
        Preconditions.checkArgument(numPulses > 0, "Please provide a proper number of pulses to be dilvered");

        ProcessingContext processingContext = ProcessingContext.builder()
            .build();
        ScheduledExecutorService executor = Executors.newSingleThreadScheduledExecutor();
        AtomicLong numGeneratedPulses = new AtomicLong(0);
        ImmutableList.Builder<EventSet> listBuilder = ImmutableList.builder();
        CompletableFuture<Void> pulsesDelivered = new CompletableFuture<>();
        ScheduledFuture<?> pulseGenFuture = executor.scheduleAtFixedRate(() -> {
            try {
                synchronized (processor) {
                    listBuilder.add(
                        EventSet.eventFromEventBuilder()
                            .events(processor.timeTriggerHandler(processingContext))
                            .isAggregate(true)
                            .build());
                }
            } catch (ProcessingException e) {
                throw new RuntimeException(e);
            }
            if (numGeneratedPulses.incrementAndGet() == numPulses) {
                pulsesDelivered.complete(null);
            }
        }, 0, pulseDelay, TimeUnit.MILLISECONDS);
        EventCollector eventCollector = new EventCollector(processingContext);
        for (EventSet eventSet : incomingEvents) {
            synchronized (processor) {
                process(processor, processingContext, eventCollector, eventSet);
            }
        }
        pulsesDelivered.get();
        pulseGenFuture.cancel(true);
        return listBuilder.build();
    }

    public List<EventSet> runStreamingProcessor(final StreamingProcessor processor, List<EventSet> incomingEvents)
            throws Exception {
        Preconditions.checkNotNull(processor, "Processor can't be null!!");
        Preconditions
                .checkNotNull(incomingEvents, "Please provide events to be sent to the processor.consume() method");
        Preconditions.checkArgument(!incomingEvents.isEmpty(),
                "Please provide events to be sent to the processor.consume() method");

        ProcessingContext processingContext = ProcessingContext.builder()
            .build();
        ImmutableList.Builder<EventSet> listBuilder = ImmutableList.builder();
        EventCollector eventCollector = new EventCollector(processingContext);
        for (EventSet eventSet : incomingEvents) {
            process(processor, processingContext, eventCollector, eventSet);
            listBuilder.add(eventCollector.getEvents());
        }
        return listBuilder.build();
    }

    /**
     * A blocking method for running long running tests on an event-driven processor.
     *
     * @param processor      - an instance of StreamingProcessor which has been initialized already
     * @param incomingEvents - a list of event sets
     * @param n              - number of iterations of the test
     * @throws Exception
     */
    public void runStreamingProcessor(final StreamingProcessor processor, final List<EventSet> incomingEvents, int n)
            throws Exception {
        Preconditions.checkNotNull(processor, "Processor can't be null!!");
        Preconditions
                .checkNotNull(incomingEvents, "Please provide events to be sent to the processor.consume() method");
        Preconditions.checkArgument(!incomingEvents.isEmpty(),
                "Please provide events to be sent to the processor.consume() method");

        startReporter();
        ProcessingContext processingContext = ProcessingContext.builder()
            .build();
        EventCollector eventCollector = new EventCollector(processingContext);
        for (int i = 0; i < n; i++) {
            for (EventSet eventSet : incomingEvents) {
                process(processor, processingContext, eventCollector, eventSet);
            }
        }
        stopReporter();
    }

    /**
     * A non-blocking method for running long running tests on an event-driven processor.
     *
     * @param processor - an instance of {@code StreamingProcessor} that has been initialized already
     * @param queue     - a {@Code LinkedBlockingQueue} from which the processor consumes event sets
     * @return an instance of {@code Future<Void>} which can be cancelled when the test is complete
     * Example:
     * <pre>
     *     {@code future.cancel(true)}
     * </pre>
     */
    public Future<Void> runStreamingProcessor(final StreamingProcessor processor,
            final LinkedBlockingQueue<EventSet> queue) {
        Preconditions.checkNotNull(processor, "Processor cannot be null");
        Preconditions.checkNotNull(queue, "Queue cannot be null");

        startReporter();
        ProcessingContext processingContext = ProcessingContext.builder()
            .build();
        EventCollector eventCollector = new EventCollector(processingContext);

        ExecutorService executorService = Executors.newSingleThreadExecutor();
        Future<Void> consumerFuture = executorService.submit(() -> {
            while (true) {
                EventSet eventSet;
                try {
                    eventSet = queue.take();
                    process(processor, processingContext, eventCollector, eventSet);
                } catch (InterruptedException e) {
                    break;
                } catch (Exception e) {
                    throw new RuntimeException(e);
                } finally {
                    stopReporter();
                }
            }
            return null;
        });
        executorService.shutdown();
        return consumerFuture;
    }

    private void process(final ProcessorBase processor, final ProcessingContext processingContext,
                         final EventCollector eventCollector, final EventSet eventSet) throws ProcessingException {
        long start = System.currentTimeMillis();
        processor.process(processingContext, eventCollector, eventSet);
        long end = System.currentTimeMillis();
        if (null != metricRegistry) {
            metricRegistry.timer("consume-timer").update(end - start, TimeUnit.MILLISECONDS);
        }
    }
}

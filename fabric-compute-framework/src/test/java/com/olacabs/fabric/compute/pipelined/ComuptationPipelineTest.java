package com.olacabs.fabric.compute.pipelined;

import com.codahale.metrics.ConsoleReporter;
import com.codahale.metrics.SharedMetricRegistries;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.ImmutableList;
import com.olacabs.fabric.compute.builder.Linker;
import com.olacabs.fabric.compute.builder.impl.RegisteringLoader;
import com.olacabs.fabric.compute.pipeline.ComputationPipeline;
import com.olacabs.fabric.model.computation.ComponentInstance;
import com.olacabs.fabric.model.computation.ComputationSpec;
import com.olacabs.fabric.model.computation.Connection;
import com.olacabs.fabric.model.common.ComponentMetadata;
import com.olacabs.fabric.model.common.ComponentType;
import com.olacabs.fabric.model.event.Event;
import org.junit.Test;

import java.util.Properties;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

/**
 * Created by santanu.s on 12/09/15.
 */
public class ComuptationPipelineTest {

    @Test
    public void testCheck() throws Exception {
        Properties properties = new Properties();
        properties.put("processor.counter_1.triggering_frequency", "1000");
        properties.put("processor.summer_1.triggering_frequency", "1000");
        properties.put("computation.shutdown.wait_time_in_seconds", "1");
        properties.put("computation.channel.channel_type", " disruptor");
        properties.put("computation.disruptor.buffer_size", "64");
        properties.put("computation.disruptor.wait_strategy", "Yield ");

        final String sourceId = "source_1";
        final String pid1 = "summer_1";
        final String pid2 = "counter_1";
        final String pid3 = "printer_1";

        RegisteringLoader loader = RegisteringLoader.builder()
                .source("memory", new MemoryBasedPipelineStreamPipelineSource())
                .stage("printer", new PrinterStreamingProcessor())
                .stage("summer", new SummingProcessor())
                .stage("counter", new CountingProcessor())
                .build();

        ComputationSpec spec = ComputationSpec.builder()
                .name("test-pipeline")
                .source(
                        ComponentInstance.builder()
                                .id(sourceId)
                                .meta(
                                        ComponentMetadata.builder()
                                                .type(ComponentType.SOURCE)
                                                .id(sourceId)
                                                .name("memory")
                                                .build())
                                .build())
                .processor(
                        ComponentInstance.builder()
                                .id(pid1)
                                .meta(
                                        ComponentMetadata.builder()
                                                .type(ComponentType.PROCESSOR)
                                                .id(pid1)
                                                .name("summer")
                                                .build())
                                .build())
                .processor(
                        ComponentInstance.builder()
                                .id(pid2)
                                .meta(
                                        ComponentMetadata.builder()
                                                .type(ComponentType.PROCESSOR)
                                                .id(pid2)
                                                .name("counter")
                                                .build())
                                .build())
                        .processor(ComponentInstance.builder()
                                .id(pid3)
                                .meta(
                                        ComponentMetadata.builder()
                                                .type(ComponentType.PROCESSOR)
                                                .id(pid3)
                                                .name("printer")
                                                .build())
                                .build())
                .connection(Connection.builder().fromType(ComponentType.SOURCE).from(sourceId).to(pid1).build())
                .connection(Connection.builder().fromType(ComponentType.SOURCE).from(sourceId).to(pid2).build())
                .connection(Connection.builder().fromType(ComponentType.SOURCE).from(sourceId).to(pid3).build())
                .connection(Connection.builder().fromType(ComponentType.PROCESSOR).from(pid1).to(pid3).build())
                .connection(Connection.builder().fromType(ComponentType.PROCESSOR).from(pid2).to(pid3).build())
                .properties(properties)
                                .build();
        System.out.println(new ObjectMapper().writerWithDefaultPrettyPrinter().writeValueAsString(spec));

        Linker linker = new Linker(loader);
        ComputationPipeline pipeline = linker.build(spec);
        pipeline.initialize(properties);

        ExecutorService executor = Executors.newSingleThreadExecutor();

        ConsoleReporter reporter = ConsoleReporter.forRegistry(SharedMetricRegistries.getOrCreate("metrics-registry"))
                .convertRatesTo(TimeUnit.SECONDS)
                .convertDurationsTo(TimeUnit.MILLISECONDS)
                .build();
        reporter.start(1, TimeUnit.SECONDS);
        executor.submit(pipeline::start);
        Thread.sleep(2000);
        pipeline.stop();
        reporter.stop();

        executor.shutdownNow();

    }

}
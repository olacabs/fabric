package com.olacabs.fabric.compute.pipelined;

import com.olacabs.fabric.compute.builder.Linker;
import com.olacabs.fabric.compute.builder.impl.RegisteringLoader;
import com.olacabs.fabric.compute.pipeline.ComputationPipeline;
import com.olacabs.fabric.model.common.ComponentMetadata;
import com.olacabs.fabric.model.common.ComponentType;
import com.olacabs.fabric.model.computation.ComponentInstance;
import com.olacabs.fabric.model.computation.ComputationSpec;
import com.olacabs.fabric.model.computation.Connection;
import junit.framework.Assert;
import org.junit.Ignore;
import org.junit.Test;

import java.util.Properties;
import java.util.concurrent.*;

/**
 * Created by santanu.s on 12/09/15.
 */
public class ForkedComputationPipelineTest {

    @Test
    @Ignore
    public void testCheck() throws Exception {
        Properties properties = new Properties();
        properties.setProperty("computation.shutdown.wait_time_in_seconds", "5");
        properties.put("computation.channel.channel_type", " disruptor");
        properties.put("computation.disruptor.buffer_size", "64");
        properties.put("computation.disruptor.wait_strategy", "Yield ");

        RegisteringLoader loader = RegisteringLoader.builder()
                .source("memory", new MemoryBasedPipelineStreamPipelineSource())
                .stage("generator", new EventGeneratorProcessor())
                .stage("printer", new PrinterStreamingProcessor())
                .build();

        Linker linker = new Linker(loader);

        final String sourceId = "source_1";
        final String pid1 = "generator_1";
        final String pid2 = "printer_1";

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
                                                .name("generator")
                                                .build())
                                .build())
                .processor(
                        ComponentInstance.builder()
                                .id(pid2)
                                .meta(
                                        ComponentMetadata.builder()
                                                .type(ComponentType.PROCESSOR)
                                                .id(pid2)
                                                .name("printer")
                                                .build())
                                .build())
                .connection(Connection.builder().fromType(ComponentType.SOURCE).from(sourceId).to(pid1).build())
                .connection(Connection.builder().fromType(ComponentType.PROCESSOR).from(pid1).to(pid2).build())
                .properties(properties)
                .build();
        ComputationPipeline pipeline = linker.build(spec);
        pipeline.initialize(properties);

        ExecutorService executor = Executors.newSingleThreadExecutor();

        executor.submit(pipeline::start);

        for (int i = 0; i < 10; i++) {
            Assert.assertTrue(pipeline.healthcheck());
        }

        Thread.sleep(2000);

        pipeline.stop();
        executor.shutdownNow();
    }

}
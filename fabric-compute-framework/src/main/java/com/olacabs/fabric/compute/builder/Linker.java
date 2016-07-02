package com.olacabs.fabric.compute.builder;

import com.codahale.metrics.MetricRegistry;
import com.codahale.metrics.SharedMetricRegistries;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.olacabs.fabric.compute.pipeline.ComputationPipeline;
import com.olacabs.fabric.compute.pipeline.NotificationBus;
import com.olacabs.fabric.compute.pipeline.PipelineStage;
import com.olacabs.fabric.compute.processor.ProcessorBase;
import com.olacabs.fabric.compute.source.PipelineStreamSource;
import com.olacabs.fabric.model.common.ComponentMetadata;
import com.olacabs.fabric.model.computation.ComputationSpec;
import com.olacabs.fabric.compute.ProcessingContext;
import com.olacabs.fabric.compute.source.PipelineSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Created by santanu.s on 19/09/15.
 */
public class Linker {
    private static final Logger logger = LoggerFactory.getLogger(Linker.class);

    private static final String DEFAULT_REGISTRY_NAME="metrics-registry";

    private final Loader loader;
    private MetricRegistry metricRegistry;

    public Linker(Loader loader) {
        this(loader, SharedMetricRegistries.getOrCreate(DEFAULT_REGISTRY_NAME));
    }
    public Linker(Loader loader, MetricRegistry metricRegistry) {
        this.loader = loader;
        this.metricRegistry = metricRegistry;
    }

    public ComputationPipeline build(ComputationSpec spec) {
        final NotificationBus notificationBus = new NotificationBus(spec.getProperties());
        final ProcessingContext processingContext = new ProcessingContext();
        processingContext.setTopologyName(spec.getName());
        ComputationPipeline pipeline = ComputationPipeline.builder();
        pipeline.notificationBus(notificationBus);
        pipeline.computationName(spec.getName());
        Map<String, PipelineStreamSource> sources = Maps.newHashMap();
        Map<String, PipelineStage> stages = Maps.newHashMap();
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.setSerializationInclusion(JsonInclude.Include.NON_EMPTY);
        objectMapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);
        spec.getSources().forEach(sourceMetadata -> {
            final ComponentMetadata meta = sourceMetadata.getMeta();
            PipelineSource source = null;
            final String errorMessage = String.format("Source object not loaded properly [%s:%s:%s]",
                    meta.getNamespace(), meta.getName(), meta.getVersion());
            try {
                PipelineSource sourceCopy = loader.loadSource(meta);
                if (sourceCopy != null) {
                    source = sourceCopy.getClass().getDeclaredConstructor().newInstance();
                }
            } catch (Exception e) {
                throw new RuntimeException(errorMessage, e);
            }
            Preconditions.checkNotNull(source, errorMessage);
            logger.info("Loaded source: {}:{}:{}",
                    meta.getNamespace(), meta.getName(), meta.getVersion());
            PipelineStreamSource sourceStage = PipelineStreamSource.builder()
                            .instanceId(sourceMetadata.getId())
                            .properties(sourceMetadata.getProperties())
                            .notificationBus(notificationBus)
                            .sourceMetadata(sourceMetadata.getMeta())
                            .source(source)
                            .processingContext(processingContext)
                            .objectMapper(objectMapper)
                            .registry(metricRegistry)
                            .build();
            pipeline.addSource(sourceStage);
            sources.put(sourceMetadata.getId(), sourceStage);
        });
        spec.getProcessors().forEach(processorMetadata -> {
            final ComponentMetadata meta = processorMetadata.getMeta();
            ProcessorBase processorBase = null;
            final String errorMessage = String.format("Processor object not loaded properly [%s:%s:%s]",
                    meta.getNamespace(), meta.getName(), meta.getVersion());
            try {
                ProcessorBase processorBaseCopy = loader.loadProcessor(meta);
                if (processorBaseCopy != null) {
                    processorBase = processorBaseCopy.getClass().getDeclaredConstructor().newInstance();
                }
            } catch (Exception e) {
                throw new RuntimeException(errorMessage, e);
            }
            Preconditions.checkNotNull(processorBase, errorMessage);
            logger.info("Loaded processor: {}:{}:{}",
                    meta.getNamespace(), meta.getName(), meta.getVersion());

            PipelineStage stage = PipelineStage.builder()
                    .instanceId(processorMetadata.getId())
                    .properties(processorMetadata.getProperties())
                    .notificationBus(notificationBus)
                    .processorMetadata(processorMetadata.getMeta())
                    .processor(processorBase)
                    .context(processingContext)
                    .build();
            pipeline.addPipelineStage(stage);
            stages.put(processorMetadata.getId(), stage);
        });
        spec.getConnections().forEach(connection -> {
            switch (connection.getFromType()) {
                case SOURCE: {
                    pipeline.connect(sources.get(connection.getFrom()), stages.get(connection.getTo()));
                    break;
                }
                case PROCESSOR: {
                    pipeline.connect(stages.get(connection.getFrom()), stages.get(connection.getTo()));
                    break;
                }
            }
        });
        return pipeline;
    }
}

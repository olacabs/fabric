package com.olacabs.fabric.compute.builder.impl;

import com.olacabs.fabric.compute.builder.Loader;
import com.olacabs.fabric.compute.processor.ProcessorBase;
import com.olacabs.fabric.model.common.ComponentMetadata;
import com.olacabs.fabric.compute.source.PipelineSource;
import lombok.Builder;
import lombok.Singular;

import java.util.Map;

/**
 * Created by santanu.s on 19/09/15.
 */
@Builder
public class RegisteringLoader implements Loader {
    @Singular
    private Map<String, PipelineSource> sources;

    @Singular
    private Map<String, ProcessorBase> stages;

    @Override
    public PipelineSource loadSource(ComponentMetadata source) {
        return sources.get(source.getName());
    }

    @Override
    public ProcessorBase loadProcessor(ComponentMetadata processor) {
        return stages.get(processor.getName());
    }

    @Override
    public int getSourceCount() {
        return sources.size();
    }

    @Override
    public int getProcessorCount() {
        return stages.size();
    }
}

package com.olacabs.fabric.compute.builder;

import com.olacabs.fabric.compute.processor.ProcessorBase;
import com.olacabs.fabric.model.common.ComponentMetadata;
import com.olacabs.fabric.compute.source.PipelineSource;

/**
 * Created by santanu.s on 19/09/15.
 */
public interface Loader {
    PipelineSource loadSource(ComponentMetadata source);

    ProcessorBase loadProcessor(ComponentMetadata processor);

    int getSourceCount();

    int getProcessorCount();
}

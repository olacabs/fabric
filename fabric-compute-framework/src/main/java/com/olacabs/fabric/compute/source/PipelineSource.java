package com.olacabs.fabric.compute.source;


import com.olacabs.fabric.compute.ProcessingContext;
import com.olacabs.fabric.model.common.ComponentMetadata;
import com.olacabs.fabric.model.event.RawEventBundle;

import java.util.Properties;

/**
 * Created by santanu.s on 20/09/15.
 */
public interface PipelineSource {

    void initialize(String instanceId, Properties globalProperties, Properties properties,
                    ProcessingContext processingContext, ComponentMetadata componentMetadata) throws Exception;

    //Return old tuple
    RawEventBundle getNewEvents();

    default void ack(RawEventBundle rawEventBundle) {
    }

    default boolean healthcheck() {
        return true;
    }

}

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

package com.olacabs.fabric.compute.source;


import com.olacabs.fabric.compute.ProcessingContext;
import com.olacabs.fabric.model.common.ComponentMetadata;
import com.olacabs.fabric.model.common.PropertyConstraint;
import com.olacabs.fabric.model.event.RawEventBundle;

import java.util.Collections;
import java.util.List;
import java.util.Properties;

/**
 * TODO doc.
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

    default List<PropertyConstraint> getPropertyConstraints() {
        return Collections.emptyList();
    }

}

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

package com.olacabs.fabric.compute.builder.impl;

import com.olacabs.fabric.compute.builder.Loader;
import com.olacabs.fabric.compute.processor.ProcessorBase;
import com.olacabs.fabric.compute.source.PipelineSource;
import com.olacabs.fabric.model.common.ComponentMetadata;
import lombok.Builder;
import lombok.Singular;

import java.util.Map;

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

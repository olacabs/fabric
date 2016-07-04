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

package com.olacabs.fabric.compute.builder;

import com.google.common.collect.ImmutableSet;
import com.olacabs.fabric.compute.builder.impl.ArtifactoryJarPathResolver;
import com.olacabs.fabric.model.common.ComponentSource;
import com.olacabs.fabric.model.common.ComponentSourceVisitor;
import com.olacabs.fabric.model.common.sources.ArtifactoryComponentSource;
import com.olacabs.fabric.model.common.sources.JarComponentSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collection;

/**
 * Created by santanu.s on 02/10/15.
 */
public class ComponentUrlResolver implements ComponentSourceVisitor {

    private static Logger logger = LoggerFactory.getLogger(ComponentUrlResolver.class);
    private ImmutableSet.Builder<String> listBuilder = ImmutableSet.builder();

    public static Collection<String> urls(Collection<ComponentSource> sources) {
        ComponentUrlResolver resolver = new ComponentUrlResolver();
        sources.forEach(source -> source.accept(resolver));
        return resolver.listBuilder.build();
    }

    @Override
    public void visit(ArtifactoryComponentSource artifactoryComponentSource) {
        try {
            logger.info("Resolving artifactory url with {} , {}, {}, {}", artifactoryComponentSource.getArtifactoryUrl(),
                artifactoryComponentSource.getGroupId(),
                artifactoryComponentSource.getArtifactId(),
                artifactoryComponentSource.getVersion());
            listBuilder.add(
                ArtifactoryJarPathResolver.resolve(
                    artifactoryComponentSource.getArtifactoryUrl(),
                    artifactoryComponentSource.getGroupId(),
                    artifactoryComponentSource.getArtifactId(),
                    artifactoryComponentSource.getVersion()));
        } catch (Exception e) {
            logger.error("Runtime Exception  ", e);
            throw new RuntimeException(e);
        }
    }

    @Override
    public void visit(JarComponentSource jarComponentSource) {
        listBuilder.add(jarComponentSource.getUrl());
    }
}

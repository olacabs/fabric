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

    @Override
    public void visit(ArtifactoryComponentSource artifactoryComponentSource) {
        try {
            logger.info("Resolving artifactory url with {} , {}, {}, {}" , artifactoryComponentSource.getArtifactoryUrl(),
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

    public static Collection<String> urls(Collection<ComponentSource> sources) {
        ComponentUrlResolver resolver = new ComponentUrlResolver();
        sources.forEach(source -> source.accept(resolver));
        return resolver.listBuilder.build();
    }
}

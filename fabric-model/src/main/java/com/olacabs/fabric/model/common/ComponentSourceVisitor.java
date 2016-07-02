package com.olacabs.fabric.model.common;

import com.olacabs.fabric.model.common.sources.ArtifactoryComponentSource;
import com.olacabs.fabric.model.common.sources.JarComponentSource;

/**
 * Created by santanu.s on 02/10/15.
 */
public interface ComponentSourceVisitor {
    void visit(ArtifactoryComponentSource artifactoryComponentSource);

    void visit(JarComponentSource jarComponentSource);
}

package com.olacabs.fabric.model.common;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.olacabs.fabric.model.common.sources.ArtifactoryComponentSource;
import com.olacabs.fabric.model.common.sources.JarComponentSource;
import lombok.Getter;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;

/**
 * Represents a source from which the jar for a component can be downloaded.
 */
@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, include = JsonTypeInfo.As.PROPERTY, property = "type")
@JsonSubTypes({
    @JsonSubTypes.Type(value = JarComponentSource.class, name = ComponentSourceType.JAR),
    @JsonSubTypes.Type(value = ArtifactoryComponentSource.class, name = ComponentSourceType.ARTIFACTORY),
})
public abstract class ComponentSource {
    @JsonIgnore
    @Getter
    @NotNull
    private final ComponentSourceType type;

    protected ComponentSource(ComponentSourceType type) {
        this.type = type;
    }

    abstract public void accept(ComponentSourceVisitor visitor);
}

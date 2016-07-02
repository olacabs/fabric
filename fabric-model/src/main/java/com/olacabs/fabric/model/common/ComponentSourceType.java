package com.olacabs.fabric.model.common;

import lombok.Getter;
import org.hibernate.validator.constraints.NotEmpty;

import javax.validation.constraints.NotNull;

/**
 * Created by santanu.s on 02/10/15.
 */
public enum ComponentSourceType {
    artifactory(ComponentSourceType.ARTIFACTORY),
    jar(ComponentSourceType.JAR);

    public static final String ARTIFACTORY = "artifactory";
    public static final String JAR = "jar";

    @Getter
    @NotNull
    @NotEmpty
    private final String name;

    ComponentSourceType(String name) {
        this.name = name;
    }


}

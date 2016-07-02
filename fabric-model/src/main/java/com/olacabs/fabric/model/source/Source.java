package com.olacabs.fabric.model.source;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * Created by santanu.s on 08/09/15.
 */
@Retention(RetentionPolicy.RUNTIME)
@JsonIgnoreProperties(ignoreUnknown = true)
public @interface Source {
    String namespace() default "global";
    String name();
    String version();
    String description();
    double cpu();
    double memory();
    String[] requiredProperties() default {};
    String[] optionalProperties() default {};
}

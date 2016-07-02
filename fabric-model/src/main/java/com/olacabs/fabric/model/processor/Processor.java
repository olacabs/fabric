package com.olacabs.fabric.model.processor;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * Created by santanu.s on 08/09/15.
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@JsonIgnoreProperties(ignoreUnknown = true)
public @interface Processor {
    String namespace() default "global";

    String name();

    String version();

    String description();

    double cpu();

    double memory();

    ProcessorType processorType();

    @Deprecated String[] requiredFields() default {};

    String[] requiredProperties() default {};

    String[] optionalProperties() default {};
}

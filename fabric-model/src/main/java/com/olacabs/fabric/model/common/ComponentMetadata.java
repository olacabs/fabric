package com.olacabs.fabric.model.common;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.olacabs.fabric.model.processor.ProcessorType;
import lombok.*;
import org.hibernate.validator.constraints.NotEmpty;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by santanu.s on 09/09/15.
 */
@Builder
@AllArgsConstructor
@NoArgsConstructor
@JsonIgnoreProperties (ignoreUnknown = true)
public class ComponentMetadata {
    @Getter
    @Setter
    @NotNull
    private String id;

    @Getter
    @Setter
    @NotNull
    private ComponentType type;

    @Getter
    @Setter
    @NotNull
    @NotEmpty
    private String namespace;

    @Getter
    @Setter
    @NotNull
    @NotEmpty
    private String name;

    @Getter
    @Setter
    @NotNull
    @NotEmpty
    private String version;

    @Getter
    @Setter
    private String description;

    @Getter
    @Setter
    private ProcessorType processorType;

    @Getter
    @Setter
    @Deprecated
    private List<String> requiredFields = new ArrayList<>();

    @Getter
    @Setter
    private List<String> requiredProperties = new ArrayList<>();

    @Getter
    @Setter
    private List<String> optionalProperties = new ArrayList<>();

    @Getter
    @Setter
    private double cpu;

    @Getter
    @Setter
    private double memory;

    @Getter
    @Setter
    @NotNull
    private ComponentSource source;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        ComponentMetadata that = (ComponentMetadata) o;

        return id.equals(that.id);

    }

    @Override
    public int hashCode() {
        return id.hashCode();
    }
}

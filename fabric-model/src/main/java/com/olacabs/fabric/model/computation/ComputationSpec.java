package com.olacabs.fabric.model.computation;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.google.common.collect.Maps;
import lombok.*;
import org.hibernate.validator.constraints.Email;
import org.hibernate.validator.constraints.NotEmpty;

import javax.validation.constraints.NotNull;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

/**
 * Created by santanu.s on 19/09/15.
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class ComputationSpec {
    private String id;

    @NotNull
    @NotEmpty
    private String name;
    @NotNull
    @Email
    private String email;

    @NotNull
    private String description;

    @Singular
    @NotNull
    private Map<String, String> attributes = Maps.newHashMap();

    @Singular
    @NotNull
    @NotEmpty
    private Set<ComponentInstance> sources;

    @Singular
    @NotNull
    @NotEmpty
    private Set<ComponentInstance> processors;

    @Singular
    @NotNull
    @NotEmpty
    private List<Connection> connections;

    Properties properties = new Properties();
}

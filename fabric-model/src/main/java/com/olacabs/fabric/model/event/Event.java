package com.olacabs.fabric.model.event;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.databind.JsonNode;
import lombok.*;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by santanu.s on 08/09/15.
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class Event {
    private long id;
    private Object data;
    @JsonIgnore
    private JsonNode jsonNode;
    private Map<String, String> properties;

    public Map<String, String> getProperties() {
        if (null == properties) {
            properties = new HashMap<>();
        }
        return properties;
    }
}

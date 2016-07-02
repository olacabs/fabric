package com.olacabs.fabric.model.computation;

import com.olacabs.fabric.model.common.ComponentMetadata;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Properties;

/**
 * Created by santanu.s on 05/10/15.
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ComponentInstance {
    private String id;
    private Properties properties = new Properties();
    private ComponentMetadata meta;
}

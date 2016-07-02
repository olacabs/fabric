package com.olacabs.fabric.model.computation;

import com.olacabs.fabric.model.common.ComponentType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Created by santanu.s on 19/09/15.
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class Connection {
    private ComponentType fromType;
    private String from;
    private String to;
}

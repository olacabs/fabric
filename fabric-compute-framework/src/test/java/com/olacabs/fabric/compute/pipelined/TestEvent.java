package com.olacabs.fabric.compute.pipelined;

import com.olacabs.fabric.model.event.Event;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * Created by santanu.s on 10/09/15.
 */
@EqualsAndHashCode(callSuper = false)
@Data
@AllArgsConstructor
public class TestEvent extends Event {
    private String marker;
    private int value;
}

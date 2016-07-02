package com.olacabs.fabric.compute.comms;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Created by santanu.s on 10/09/15.
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class CommsFrameworkMessage<T> {
    private long id;
    private String source;
    private T payload;

    public static <T> void translate(CommsFrameworkMessage<T> message, long sequence, CommsFrameworkMessage<T> original) {
        message.setId(original.getId());
        message.setSource(original.getSource());
        message.setPayload(original.getPayload());
    }
}

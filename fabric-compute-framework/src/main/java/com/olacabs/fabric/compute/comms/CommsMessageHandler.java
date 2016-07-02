package com.olacabs.fabric.compute.comms;

/**
 * Created by santanu.s on 10/09/15.
 */
public interface CommsMessageHandler<T> {
    String name();
    void handlePipelineMessage(T message) throws Exception;
}

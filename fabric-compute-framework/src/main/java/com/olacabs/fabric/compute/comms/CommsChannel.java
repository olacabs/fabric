package com.olacabs.fabric.compute.comms;

/**
 * Created by santanu.s on 10/09/15.
 */
public interface CommsChannel<EventType> {

    String name();

    void publish(EventType eventType);

    void start();

    void stop();
}

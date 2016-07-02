package com.olacabs.fabric.compute.sources.kafka.impl;

/**
 * Created by santanu.s on 14/10/15.
 */
public class BrokerQueryException extends Exception {
    public BrokerQueryException(String message) {
        super(message);
    }

    public BrokerQueryException(String message, Throwable cause) {
        super(message, cause);
    }
}

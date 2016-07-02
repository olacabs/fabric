package com.olacabs.fabric.compute.comms;

/**
 * Created by guruprasad.sridharan on 26/06/16.
 */
public enum DisruptorWaitStrategy {
    BLOCK,
    LITE,
    TIMEOUT,
    SLEEP,
    YIELD,
    BUSY
}

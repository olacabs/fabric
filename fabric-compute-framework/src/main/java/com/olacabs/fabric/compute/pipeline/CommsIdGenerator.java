package com.olacabs.fabric.compute.pipeline;

/**
 * Created by santanu.s on 12/09/15.
 */
public class CommsIdGenerator {
    private static int id = 0;

    public static int nextId() {
        return id++;
    }
}

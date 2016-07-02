package com.olacabs.fabric.compute.sources.kafka.impl;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.InetAddress;
import java.net.UnknownHostException;

/**
 * Created by santanu.s on 07/10/15.
 */
public class HostUtils {
    private static final Logger logger = LoggerFactory.getLogger(HostUtils.class);

    private static String hostname;

    static {
        try {
            hostname = InetAddress.getLocalHost().getHostAddress();
        } catch (UnknownHostException e) {
            logger.error("Could not find host IP: ", e);
            hostname = "localhost";
        }
    }

    public static void hostname(final String inHostname) {
        hostname = inHostname;
    }

    public static String hostname() {
        return hostname;
    }

}

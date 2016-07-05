/*
 * Copyright 2016 ANI Technologies Pvt. Ltd.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.olacabs.fabric.compute.sources.kafka.impl;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.InetAddress;
import java.net.UnknownHostException;

/**
 * TODO.
 */
public final class HostUtils {

    private HostUtils() {

    }
    private static final Logger LOGGER = LoggerFactory.getLogger(HostUtils.class);

    private static String hostname;

    static {
        try {
            hostname = InetAddress.getLocalHost().getHostAddress();
        } catch (UnknownHostException e) {
            LOGGER.error("Could not find host IP: ", e);
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

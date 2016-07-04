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

package com.olacabs.fabric.common.util;

import com.google.common.base.Strings;

import java.util.Properties;

public class PropertyReader {
    public static Boolean readBoolean(Properties properties, Properties globalProperties, final String propertyName) {
        return readBoolean(properties, globalProperties, propertyName, null);
    }

    public static Boolean readBoolean(Properties properties, Properties globalProperties, final String propertyName, Boolean defaultValue) {
        String repr = null;
        if (null != properties) {
            repr = properties.getProperty(propertyName);
        }
        if (Strings.isNullOrEmpty(repr)) {
            if (null != globalProperties) {
                repr = globalProperties.getProperty(propertyName);
            }
            if (Strings.isNullOrEmpty(repr)) {
                return defaultValue;
            }
        }
        return Boolean.parseBoolean(repr);
    }

    public static Integer readInt(Properties properties, Properties globalProperties, final String propertyName) {
        return readInt(properties, globalProperties, propertyName, null);
    }

    public static Integer readInt(Properties properties, Properties globalProperties, final String propertyName, Integer defaultValue) {
        String repr = null;
        if (null != properties) {
            repr = properties.getProperty(propertyName);
        }
        if (Strings.isNullOrEmpty(repr)) {
            if (null != globalProperties) {
                repr = globalProperties.getProperty(propertyName);
            }
            if (Strings.isNullOrEmpty(repr)) {
                return defaultValue;
            }
        }
        return Integer.parseInt(repr);
    }

    public static Long readLong(Properties properties, Properties globalProperties, final String propertyName) {
        return readLong(properties, globalProperties, propertyName, null);
    }

    public static Long readLong(Properties properties, Properties globalProperties, final String propertyName, Long defaultValue) {
        String repr = null;
        if (null != properties) {
            repr = properties.getProperty(propertyName);
        }
        if (Strings.isNullOrEmpty(repr)) {
            if (null != globalProperties) {
                repr = globalProperties.getProperty(propertyName);
            }
            if (Strings.isNullOrEmpty(repr)) {
                return defaultValue;
            }
        }
        return Long.parseLong(repr);
    }

    public static String readString(Properties properties, Properties globalProperties, final String propertyName) {
        return readString(properties, globalProperties, propertyName, null);
    }

    public static String readString(Properties properties, Properties globalProperties, final String propertyName, String defaultValue) {
        String repr = properties.getProperty(propertyName);
        if (Strings.isNullOrEmpty(repr)) {
            repr = globalProperties.getProperty(propertyName);
            if (Strings.isNullOrEmpty(repr)) {
                return defaultValue;
            }
        }
        return repr;
    }
}

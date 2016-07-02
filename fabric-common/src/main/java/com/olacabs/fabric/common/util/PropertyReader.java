package com.olacabs.fabric.common.util;

import com.google.common.base.Strings;

import java.util.Properties;

/**
 * Created by santanu.s on 09/09/15.
 */
public class PropertyReader {
    public static Boolean readBoolean(Properties properties, Properties globalProperties, final String propertyName) {
        return readBoolean(properties, globalProperties, propertyName, null);
    }

    public static Boolean readBoolean(Properties properties, Properties globalProperties, final String propertyName, Boolean defaultValue) {
        String repr = null;
        if(null != properties) {
            repr = properties.getProperty(propertyName);
        }
        if(Strings.isNullOrEmpty(repr)) {
            if(null != globalProperties) {
                repr = globalProperties.getProperty(propertyName);
            }
            if(Strings.isNullOrEmpty(repr)) {
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
        if(null != properties) {
            repr = properties.getProperty(propertyName);
        }
        if(Strings.isNullOrEmpty(repr)) {
            if(null != globalProperties) {
                repr = globalProperties.getProperty(propertyName);
            }
            if(Strings.isNullOrEmpty(repr)) {
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
        if(null != properties) {
            repr = properties.getProperty(propertyName);
        }
        if(Strings.isNullOrEmpty(repr)) {
            if(null != globalProperties) {
                repr = globalProperties.getProperty(propertyName);
            }
            if(Strings.isNullOrEmpty(repr)) {
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
        if(Strings.isNullOrEmpty(repr)) {
            repr = globalProperties.getProperty(propertyName);
            if(Strings.isNullOrEmpty(repr)) {
                return defaultValue;
            }
        }
        return repr;
    }
}

package com.olacabs.fabric.compute.util;

import com.olacabs.fabric.common.util.PropertyReader;
import com.olacabs.fabric.model.common.ComponentMetadata;

import java.util.Properties;

/**
 * Created by santanu.s on 03/11/15.
 */
public class ComponentPropertyReader {

    public static String readString(Properties properties, Properties global,
                                    final String propertyName, String instanceId, ComponentMetadata componentMetadata) {
        return readString(properties, global, propertyName, instanceId, componentMetadata, null);
    }

    public static String readString(Properties properties, Properties global,
                                    final String propertyName, String instanceId,
                                    ComponentMetadata componentMetadata, String defaultValue) {
        final String key = generateKey(propertyName, instanceId, componentMetadata);
        return PropertyReader.readString(properties, global, key, defaultValue);
    }

    public static Integer readInteger(Properties properties, Properties global,
                                      final String propertyName, String instanceId, ComponentMetadata componentMetadata) {
        return readInteger(properties, global, propertyName, instanceId, componentMetadata, null);
    }

    public static Integer readInteger(Properties properties, Properties global,
                                      final String propertyName, String instanceId,
                                      ComponentMetadata componentMetadata, Integer defaultValue) {
        final String key = generateKey(propertyName, instanceId, componentMetadata);
        return PropertyReader.readInt(properties, global, key, defaultValue);
    }

    public static Long readLong(Properties properties, Properties global,
                                final String propertyName, String instanceId, ComponentMetadata componentMetadata) {
        return readLong(properties, global, propertyName, instanceId, componentMetadata, null);
    }

    public static Long readLong(Properties properties, Properties global,
                                final String propertyName, String instanceId,
                                ComponentMetadata componentMetadata, Long defaultValue) {
        final String key = generateKey(propertyName, instanceId, componentMetadata);
        return PropertyReader.readLong(properties, global, key, defaultValue);
    }

    public static Boolean readBoolean(Properties properties, Properties global,
                                final String propertyName, String instanceId, ComponentMetadata componentMetadata) {
        return readBoolean(properties, global, propertyName, instanceId, componentMetadata, null);
    }

    public static Boolean readBoolean(Properties properties, Properties global,
                                final String propertyName, String instanceId,
                                ComponentMetadata componentMetadata, Boolean defaultValue) {
        final String key = generateKey(propertyName, instanceId, componentMetadata);
        return PropertyReader.readBoolean(properties, global, key, defaultValue);
    }

    private static String generateKey(String propertyName, String instanceId, ComponentMetadata componentMetadata) {
        return String.format("%s.%s.%s",
                    componentMetadata.getType().name().toLowerCase(),
                    instanceId,
                    propertyName);
    }
}

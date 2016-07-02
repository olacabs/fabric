package com.olacabs.fabric.common.util;

/**
 * Helper methods for handling Exceptions
 */
public class ExceptionHelper {
    public static Throwable getLeafThrowable(final Throwable t) {
        Throwable tmp = t;
        Throwable current = t;
        while (current != null) {
            tmp = current;
            current = current.getCause();
        }
        return tmp;
    }

    public static String getLeafErrorMessage(final Throwable t) {
        return getLeafThrowable(t).getMessage();
    }
}
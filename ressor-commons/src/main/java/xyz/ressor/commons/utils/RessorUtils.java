package xyz.ressor.commons.utils;

import java.io.InputStream;
import java.lang.reflect.Array;
import java.util.*;

/**
 * Utility methods here can be found in most popular libraries, but we duplicate them here in order to
 * save on dependency tree size of Ressor
 */
public class RessorUtils {

    public static <T> T firstNonNull(T first, T last) {
        return first == null ? last : first;
    }

    public static Object defaultInstance(Class<?> type) {
        if (type == boolean.class || type == Boolean.class) {
            return false;
        } else if (type == char.class || type == Character.class) {
            return Character.MIN_VALUE;
        } else if (type == byte.class || type == Byte.class) {
            return (byte) 0;
        } else if (type == short.class || type == Short.class) {
            return (short) 0;
        } else if (type == int.class || type == Integer.class) {
            return 0;
        } else if (type == long.class || type == Long.class) {
            return 0L;
        } else if (type == float.class || type == Float.class) {
            return 0.0f;
        } else if (type == double.class || type == Double.class) {
            return 0.0d;
        } else if (type.isArray()) {
            return Array.newInstance(type, 0);
        } else if (type.isAssignableFrom(List.class)) {
            return Collections.emptyList();
        } else if (type.isAssignableFrom(Collection.class)) {
            return Collections.emptyList();
        } else if (type.isAssignableFrom(Set.class)) {
            return Collections.emptySet();
        } else if (type.isAssignableFrom(Map.class)) {
            return Collections.emptyMap();
        } else if (type.isAssignableFrom(NavigableMap.class)) {
            return Collections.emptyNavigableMap();
        } else if (type.isAssignableFrom(NavigableSet.class)) {
            return Collections.emptyNavigableSet();
        } else if (type.isAssignableFrom(SortedMap.class)) {
            return Collections.emptySortedMap();
        } else if (type.isAssignableFrom(SortedSet.class)) {
            return Collections.emptySortedSet();
        } else if (type.isAssignableFrom(Iterator.class)) {
            return Collections.emptyIterator();
        } else if (type.isAssignableFrom(ListIterator.class)) {
            return Collections.emptyListIterator();
        } else {
            return null;
        }
    }

    public static void silentlyClose(InputStream stream) {
        try {
            stream.close();
        } catch (Throwable ignored) { }
    }

}

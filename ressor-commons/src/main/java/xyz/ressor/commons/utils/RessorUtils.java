package xyz.ressor.commons.utils;

/**
 * Utility methods here can be found in most popular libraries, but we duplicate them here in order to
 * save on dependency tree size of Ressor
 */
public class RessorUtils {

    public static <T> T firstNonNull(T first, T last) {
        return first == null ? last : first;
    }

}

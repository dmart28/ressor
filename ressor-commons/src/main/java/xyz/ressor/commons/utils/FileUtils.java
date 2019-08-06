package xyz.ressor.commons.utils;

import java.io.IOException;

import static java.nio.charset.StandardCharsets.UTF_8;

public class FileUtils {

    public static String classpath(String location) {
        return classpath(location, FileUtils.class);
    }

    public static String classpath(String location, Class<?> clazz) {
        var s = clazz.getClassLoader().getResourceAsStream(location);
        if (s == null) {
            return null;
        } else {
            try {
                return new String(s.readAllBytes(), UTF_8);
            } catch (IOException e) {
                throw Exceptions.wrap(e);
            }
        }
    }

}

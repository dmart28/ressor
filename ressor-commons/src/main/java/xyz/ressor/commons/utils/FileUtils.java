package xyz.ressor.commons.utils;

import org.apache.commons.io.IOUtils;

import java.io.IOException;
import java.io.InputStream;

import static java.nio.charset.StandardCharsets.UTF_8;

public class FileUtils {

    public static String classpath(String location) {
        return classpath(location, FileUtils.class);
    }

    public static String classpath(String location, Class<?> clazz) {
        InputStream s = clazz.getClassLoader().getResourceAsStream(location);
        if (s == null) {
            return null;
        } else {
            try {
                return new String(IOUtils.toByteArray(s), UTF_8);
            } catch (IOException e) {
                throw Exceptions.wrap(e);
            }
        }
    }

}

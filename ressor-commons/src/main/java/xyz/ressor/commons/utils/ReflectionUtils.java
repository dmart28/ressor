package xyz.ressor.commons.utils;

import java.lang.annotation.Annotation;
import java.lang.reflect.Constructor;
import java.lang.reflect.Executable;
import java.util.ArrayList;
import java.util.List;

public class ReflectionUtils {

    public static <T extends Executable> T findAnnotatedExecutable(T[] items,
                                                                   Class<? extends Annotation> annotationType) {
        List<T> executables = findAnnotatedExecutables(items, annotationType);
        return executables.size() > 0 ? executables.get(0) : null;
    }

    public static <T extends Executable> List<T> findAnnotatedExecutables(T[] items,
                                                                          Class<? extends Annotation> annotationType) {
        List<T> result = new ArrayList<>();
        for (T i : items) {
            if (i.getAnnotation(annotationType) != null) {
                result.add(i);
            }
        }
        return result;
    }

    public static List<? extends Executable> findAnnotatedExecutables(Class<?> type,
                                                            Class<? extends Annotation> annotationType) {
        List<Executable> result = new ArrayList<>();
        result.addAll(findAnnotatedExecutables(type.getDeclaredMethods(), annotationType));
        result.addAll(findAnnotatedExecutables(type.getDeclaredConstructors(), annotationType));
        return result;
    }

    public static <T extends Executable> T findExecutable(Class<?> type, Class<?> outputType) {
        for (Constructor<?> c : type.getDeclaredConstructors()) {
            if (c.getParameterCount() == 1 && c.getParameterTypes()[0].isAssignableFrom(outputType)) {
                return (T) c;
            }
        }
        return null;
    }

}

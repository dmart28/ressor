package xyz.ressor.commons.utils;

import java.lang.annotation.Annotation;
import java.lang.reflect.Executable;
import java.util.ArrayList;
import java.util.List;

public class ReflectionUtils {

    public static <T extends Executable> T findAnnotatedExecutable(T[] items,
                                                                   Class<? extends Annotation> annotationType) {
        var executables = findAnnotatedExecutables(items, annotationType);
        return executables.size() > 0 ? executables.get(0) : null;
    }

    public static <T extends Executable> List<T> findAnnotatedExecutables(T[] items,
                                                                          Class<? extends Annotation> annotationType) {
        var result = new ArrayList<T>();
        for (var i : items) {
            if (i.getAnnotation(annotationType) != null) {
                result.add(i);
            }
        }
        return result;
    }

    public static List<? extends Executable> findAnnotatedExecutables(Class<?> type,
                                                            Class<? extends Annotation> annotationType) {
        var result = new ArrayList<Executable>();
        result.addAll(findAnnotatedExecutables(type.getDeclaredMethods(), annotationType));
        result.addAll(findAnnotatedExecutables(type.getDeclaredConstructors(), annotationType));
        return result;
    }

    public static <T extends Executable> T findExecutable(Class<?> type, int parameterCount) {
        for (var c : type.getDeclaredConstructors()) {
            if (c.getParameterCount() == parameterCount) {
                return (T) c;
            }
        }
        return null;
    }

}

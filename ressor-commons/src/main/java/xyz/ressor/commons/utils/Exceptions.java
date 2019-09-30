package xyz.ressor.commons.utils;

import java.util.function.Consumer;
import java.util.function.Function;

public abstract class Exceptions {

    public static RuntimeException wrap(Throwable t) {
        Exceptions.<RuntimeException>rethrow(t);
        return new RuntimeException();
    }

    public static <T> Consumer<T> catchingConsumer(ThrowsConsumer<T> c) {
        return t -> {
            try {
                c.accept(t);
            } catch (Throwable th) {
                throw wrap(th);
            }
        };
    }

    public static <T, R> Function<T, R> catchingFunc(ThrowsFunction<T, R> f) {
        return t -> {
            try {
                return f.apply(t);
            } catch (Throwable th) {
                throw wrap(th);
            }
        };
    }

    private static <T extends Throwable> RuntimeException rethrow(Throwable t) throws T {
        throw (T) t;
    }

    public interface ThrowsConsumer<T> {
        void accept(T t) throws Throwable;
    }

    public interface ThrowsFunction<T, R> {
        R apply(T t) throws Throwable;
    }
}

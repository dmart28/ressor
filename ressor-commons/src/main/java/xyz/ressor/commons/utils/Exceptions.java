package xyz.ressor.commons.utils;

public abstract class Exceptions {

    public static RuntimeException wrap(Throwable t) {
        Exceptions.<RuntimeException>rethrow(t);
        return new RuntimeException();
    }

    private static <T extends Throwable> RuntimeException rethrow(Throwable t) throws T {
        throw (T) t;
    }

}

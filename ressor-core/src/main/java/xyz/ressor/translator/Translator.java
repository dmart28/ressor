package xyz.ressor.translator;

import java.util.function.Function;

public interface Translator<T, R> {

    R translate(T resource);

    default <R1> Translator<T, R1> chain(Translator<R, R1> translator) {
        return resource -> translator.translate(Translator.this.translate(resource));
    }

    default <T1> Translator<T1, R> prepend(Function<T1, T> factory) {
        return resource -> Translator.this.translate(factory.apply(resource));
    }

}

package xyz.ressor.translator;

import java.util.function.Function;

/**
 * Responsible for translating raw data read by the {@link xyz.ressor.source.Source} to the actual
 * format, understandable for end user services.
 *
 * @param <T> the input type
 * @param <R> the output type
 */
public interface Translator<T, R> {

    R translate(T resource);

    Class<T> inputType();

    Class<R> outputType();

    static <T, R> Translator<T, R> define(Function<T, R> translate, Class<T> inputType, Class<R> outputType) {
        return new Translator<>() {
            @Override
            public R translate(T resource) {
                return translate.apply(resource);
            }

            @Override
            public Class<T> inputType() {
                return inputType;
            }

            @Override
            public Class<R> outputType() {
                return outputType;
            }
        };
    }

    default <R1> Translator<T, R1> chain(Translator<R, R1> translator) {
        return new Translator<>() {
            @Override
            public R1 translate(T resource) {
                return translator.translate(Translator.this.translate(resource));
            }

            @Override
            public Class<T> inputType() {
                return Translator.this.inputType();
            }

            @Override
            public Class<R1> outputType() {
                return translator.outputType();
            }
        };
    }

    default <T1> Translator<T1, R> prepend(Function<T1, T> factory, Class<T1> inputType) {
        return new Translator<>() {
            @Override
            public R translate(T1 resource) {
                return Translator.this.translate(factory.apply(resource));
            }

            @Override
            public Class<T1> inputType() {
                return inputType;
            }

            @Override
            public Class<R> outputType() {
                return Translator.this.outputType();
            }
        };
    }

}

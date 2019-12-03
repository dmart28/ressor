package xyz.ressor.translator;

import java.util.function.Function;

/**
 * Responsible for translating data value of type <b>T</b> to the value of type <b>R</b>, which is then consumed by the end user services.
 *
 * @param <T> the input type
 * @param <R> the output type
 */
public interface Translator<T, R> {

    /**
     * Performs the actual translation.
     *
     * @param resource input resource
     * @return translated instance
     */
    R translate(T resource);

    /**
     * @return input <b>T</b> class
     */
    Class<T> inputType();

    /**
     * @return output <b>R</b> class
     */
    Class<R> outputType();

    /**
     * Helper method for defining a <b>T->R</b> translator from the translation function and two data types.
     *
     * @param translate actual translator from <b>T</b> to <b>R</b>
     * @param inputType input <b>T</b> class
     * @param outputType output <b>R</b> class
     * @param <T> input type
     * @param <R> output type
     * @return complete <b>T->R</b> translator
     */
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

    /**
     * Chains the provided translator <b>R->R1</b> to the current <b>T->R</b>.
     * <p/>
     * The resulting translator is <b>T->R->R1</b>.
     *
     * @param translator translator to be chained
     * @param <R1> final result type
     * @return the chained translator
     */
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

    /**
     * Prepends the provided factory to the current translator <b>T->R</b>.
     * <p/>
     * The resulting translator is <b>T1->T->R</b>.
     *
     * @param factory prepending translator function
     * @param inputType input <b>T1</b> class
     * @param <T1> input type
     * @return the prepended translator
     */
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

package xyz.ressor;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import xyz.ressor.translator.Translator;
import xyz.ressor.translator.Translators;

import java.io.InputStream;
import java.nio.charset.Charset;

import static java.nio.charset.StandardCharsets.UTF_8;

public class Ressor {
    private static final Logger log = LoggerFactory.getLogger(Ressor.class);

    public static <T> RessorBuilder<T> builder(Class<T> type) {
        return new RessorBuilder<T>();
    }

    public static class RessorBuilder<T> {
        private Translator<InputStream, ?> translator;

        public RessorBuilder<T> yaml() {
            this.translator = Translators.inputStream2Yaml();
            return this;
        }

        public RessorBuilder<T> yamlParser() {
            this.translator = Translators.inputStream2YamlParser();
            return this;
        }

        public RessorBuilder<T> json() {
            this.translator = Translators.inputStream2Json();
            return this;
        }

        public RessorBuilder<T> jsonParser() {
            this.translator = Translators.inputStream2JsonParser();
            return this;
        }

        public RessorBuilder<T> bytes() {
            this.translator = Translators.inputStream2Bytes();
            return this;
        }

        public RessorBuilder<T> string() {
            return string(UTF_8);
        }

        public RessorBuilder<T> string(Charset charset) {
            this.translator = Translators.inputStream2String(charset);
            return this;
        }

        public RessorBuilder<T> translator(Translator<InputStream, ?> translator) {
            this.translator = translator;
            return this;
        }

        public T build() {
            return null;
        }

    }

}

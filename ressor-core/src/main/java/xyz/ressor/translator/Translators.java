package xyz.ressor.translator;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.util.zip.GZIPInputStream;

import static java.nio.charset.StandardCharsets.UTF_8;
import static xyz.ressor.commons.utils.Exceptions.catchingFunc;
import static xyz.ressor.commons.utils.Exceptions.wrap;
import static xyz.ressor.translator.Translator.define;

public abstract class Translators {
    private static final JsonFactory JSON_FACTORY = new JsonFactory();
    private static final YAMLFactory YAML_FACTORY = new YAMLFactory();
    private static final ObjectMapper JSON_MAPPER = new ObjectMapper(JSON_FACTORY);
    private static final ObjectMapper YAML_MAPPER = new ObjectMapper(YAML_FACTORY);

    public static Translator<byte[], String> bytes2String() {
        return bytes2String(UTF_8);
    }

    public static Translator<byte[], String> bytes2String(Charset charset) {
        return define(s -> new String(s, charset), byte[].class, String.class);
    }

    public static Translator<byte[], String[]> bytes2Lines(Charset charset) {
        return define(s -> new String(s, charset).split(System.lineSeparator()), byte[].class, String[].class);
    }

    public static Translator<InputStream, byte[]> inputStream2Bytes() {
        return define(s -> {
            try {
                return s.readAllBytes();
            } catch (IOException e) {
                throw wrap(e);
            }
        }, InputStream.class, byte[].class);
    }

    public static Translator<InputStream, String> inputStream2String() {
        return inputStream2String(UTF_8);
    }

    public static Translator<InputStream, String> inputStream2String(Charset charset) {
        return inputStream2Bytes().chain(bytes2String(charset));
    }

    public static Translator<InputStream, String[]> inputStream2Lines() {
        return inputStream2Lines(UTF_8);
    }

    public static Translator<InputStream, String[]> inputStream2Lines(Charset charset) {
        return inputStream2Bytes().chain(bytes2Lines(charset));
    }

    public static Translator<InputStream, JsonNode> inputStream2Json() {
        return inputStream2Json(JSON_MAPPER);
    }

    public static Translator<InputStream, JsonNode> inputStream2Json(ObjectMapper mapper) {
        return define(s -> {
            try {
                return mapper.readTree(s);
            } catch (IOException e) {
                throw wrap(e);
            }
        }, InputStream.class, JsonNode.class);
    }

    public static Translator<InputStream, JsonNode> inputStream2Yaml() {
        return define(s -> {
            try {
                return YAML_MAPPER.readTree(s);
            } catch (IOException e) {
                throw wrap(e);
            }
        }, InputStream.class, JsonNode.class);
    }

    public static Translator<InputStream, JsonParser> inputStream2JsonParser() {
        return define(s -> {
            try {
                return JSON_FACTORY.createParser(s);
            } catch (IOException e) {
                throw wrap(e);
            }
        }, InputStream.class, JsonParser.class);
    }

    public static Translator<InputStream, JsonParser> inputStream2YamlParser() {
        return define(s -> {
            try {
                return YAML_FACTORY.createParser(s);
            } catch (IOException e) {
                throw wrap(e);
            }
        }, InputStream.class, JsonParser.class);
    }

    public static <T> Translator<InputStream, T> gzipped(Translator<InputStream, T> original) {
        return original.prepend(catchingFunc(GZIPInputStream::new), InputStream.class);
    }

}

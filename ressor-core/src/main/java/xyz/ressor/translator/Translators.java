package xyz.ressor.translator;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;

import static java.nio.charset.StandardCharsets.UTF_8;
import static xyz.ressor.commons.utils.Exceptions.wrap;

public abstract class Translators {
    private static final JsonFactory JSON_FACTORY = new JsonFactory();
    private static final YAMLFactory YAML_FACTORY = new YAMLFactory();
    private static final ObjectMapper JSON_MAPPER = new ObjectMapper(JSON_FACTORY);
    private static final ObjectMapper YAML_MAPPER = new ObjectMapper(YAML_FACTORY);

    public static Translator<byte[], String> bytes2String() {
        return bytes2String(UTF_8);
    }

    public static Translator<byte[], String> bytes2String(Charset charset) {
        return s -> new String(s, charset);
    }

    public static Translator<InputStream, byte[]> inputStream2Bytes() {
        return s -> {
            try {
                return s.readAllBytes();
            } catch (IOException e) {
                throw wrap(e);
            }
        };
    }

    public static Translator<InputStream, String> inputStream2String() {
        return inputStream2String(UTF_8);
    }

    public static Translator<InputStream, String> inputStream2String(Charset charset) {
        return inputStream2Bytes().chain(bytes2String(charset));
    }

    public static Translator<InputStream, JsonNode> inputStream2Json() {
        return inputStream2Json(JSON_MAPPER);
    }

    public static Translator<InputStream, JsonNode> inputStream2Json(ObjectMapper mapper) {
        return s -> {
            try {
                return mapper.readTree(s);
            } catch (IOException e) {
                throw wrap(e);
            }
        };
    }

    public static Translator<InputStream, JsonNode> inputStream2Yaml() {
        return s -> {
            try {
                return YAML_MAPPER.readTree(s);
            } catch (IOException e) {
                throw wrap(e);
            }
        };
    }

    public static Translator<InputStream, JsonParser> inputStream2JsonParser() {
        return s -> {
            try {
                return JSON_FACTORY.createParser(s);
            } catch (IOException e) {
                throw wrap(e);
            }
        };
    }

    public static Translator<InputStream, JsonParser> inputStream2YamlParser() {
        return s -> {
            try {
                return YAML_FACTORY.createParser(s);
            } catch (IOException e) {
                throw wrap(e);
            }
        };
    }

}

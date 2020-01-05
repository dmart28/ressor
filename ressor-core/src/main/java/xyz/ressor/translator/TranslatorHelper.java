package xyz.ressor.translator;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import xyz.ressor.translator.xml.DuplicateToArrayJsonNodeDeserializer;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.util.List;
import java.util.zip.GZIPInputStream;

import static java.nio.charset.StandardCharsets.UTF_8;
import static xyz.ressor.commons.utils.Exceptions.catchingFunc;
import static xyz.ressor.commons.utils.Exceptions.wrap;
import static xyz.ressor.translator.Translator.define;

public abstract class TranslatorHelper {
    private static final JsonFactory JSON_FACTORY = new JsonFactory();
    private static final YAMLFactory YAML_FACTORY = new YAMLFactory();
    private static final ObjectMapper JSON_MAPPER = new ObjectMapper(JSON_FACTORY);
    private static final ObjectMapper YAML_MAPPER = new ObjectMapper(YAML_FACTORY);
    private static final ObjectMapper XML_MAPPER = XmlMapper.builder()
            .addModule(new SimpleModule().addDeserializer(
                    JsonNode.class,
                    new DuplicateToArrayJsonNodeDeserializer()
            )).defaultUseWrapper(false).build();

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

    public static Translator<InputStream, String> inputStream2String(Charset charset) {
        return inputStream2Bytes().chain(bytes2String(charset));
    }

    public static Translator<InputStream, String[]> inputStream2Lines(Charset charset) {
        return inputStream2Bytes().chain(bytes2Lines(charset));
    }

    public static Translator<InputStream, JsonNode> inputStream2Json() {
        return inputStream2Node(JSON_MAPPER);
    }

    public static <T> Translator<InputStream, T> inputStream2JsonObject(Class<T> type) {
        return inputStream2Object(JSON_MAPPER, type);
    }

    public static <T> Translator<InputStream, T> inputStream2XmlObject(Class<T> type) {
        return inputStream2Object(XML_MAPPER, type);
    }

    public static <T> Translator<InputStream, List<T>> inputStream2XmlObjectList(Class<T> type) {
        return inputStream2ObjectList(XML_MAPPER, type);
    }

    public static <T> Translator<InputStream, List<T>> inputStream2JsonObjectList(Class<T> type) {
        return inputStream2ObjectList(JSON_MAPPER, type);
    }

    public static <T> Translator<InputStream, List<T>> inputStream2YamlObjectList(Class<T> type) {
        return inputStream2ObjectList(YAML_MAPPER, type);
    }

    public static Translator<InputStream, JsonNode> inputStream2Xml() {
        return inputStream2Node(XML_MAPPER);
    }

    public static Translator<InputStream, JsonNode> inputStream2Yaml() {
        return inputStream2Node(YAML_MAPPER);
    }

    public static <T> Translator<InputStream, T> inputStream2YamlObject(Class<T> type) {
        return inputStream2Object(YAML_MAPPER, type);
    }

    public static Translator<InputStream, JsonParser> inputStream2XmlParser() {
        return inputStream2NodeParser(XML_MAPPER.getFactory());
    }

    public static Translator<InputStream, JsonParser> inputStream2JsonParser() {
        return inputStream2NodeParser(JSON_FACTORY);
    }

    public static Translator<InputStream, JsonParser> inputStream2YamlParser() {
        return inputStream2NodeParser(YAML_FACTORY);
    }

    public static <T> Translator<InputStream, T> gzipped(Translator<InputStream, T> original) {
        return original.prepend(catchingFunc(GZIPInputStream::new), InputStream.class);
    }

    private static <T> Translator<InputStream, T> inputStream2Object(ObjectMapper mapper, Class<T> type) {
        return define(s -> {
            try {
                return mapper.readValue(s, type);
            } catch (IOException e) {
                throw wrap(e);
            }
        }, InputStream.class, type);
    }

    private static <T> Translator<InputStream, List<T>> inputStream2ObjectList(ObjectMapper mapper, Class<T> type) {
        return define(s -> {
            try {
                var t = mapper.getTypeFactory().constructCollectionType(List.class, type);
                return mapper.readValue(s, t);
            } catch (IOException e) {
                throw wrap(e);
            }
        }, InputStream.class, (Class<List<T>>) (Class<?>) List.class);
    }

    private static Translator<InputStream, JsonNode> inputStream2Node(ObjectMapper mapper) {
        return define(s -> {
            try {
                return mapper.readTree(s);
            } catch (IOException e) {
                throw wrap(e);
            }
        }, InputStream.class, JsonNode.class);
    }

    private static Translator<InputStream, JsonParser> inputStream2NodeParser(JsonFactory f) {
        return define(s -> {
            try {
                return f.createParser(s);
            } catch (IOException e) {
                throw wrap(e);
            }
        }, InputStream.class, JsonParser.class);
    }

}

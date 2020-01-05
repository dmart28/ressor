package xyz.ressor.translator;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.util.List;

import static java.nio.charset.StandardCharsets.UTF_8;

public final class Translators {

    /**
     * Expect XML data format from the source, will provide {@link com.fasterxml.jackson.databind.JsonNode} instance
     * to the service factory.
     *
     * Please note that by default parser will not wrap root element and duplicate elements will be combined under
     * {@link com.fasterxml.jackson.databind.node.ArrayNode}, so no data is lost.
     */
    public static Translator<InputStream, JsonNode> xml() {
        return TranslatorHelper.inputStream2Xml();
    }

    /**
     * See {@link #xml()}.
     */
    public static Translator<InputStream, JsonNode> xml(ObjectMapper mapper) {
        return TranslatorHelper.inputStream2Xml(mapper);
    }

    /**
     * Expect XML data format from the source, will provide instance of entityType class to the service factory.
     *
     * @param entityType the target type class
     */
    public static <T> Translator<InputStream, T> xml(Class<T> entityType) {
        return TranslatorHelper.inputStream2XmlObject(entityType);
    }

    /**
     * See {@link #xml(Class)}.
     */
    public static <T> Translator<InputStream, T> xml(Class<T> entityType, ObjectMapper mapper) {
        return TranslatorHelper.inputStream2XmlObject(entityType, mapper);
    }

    /**
     * Same as {@link #xml(Class)}, but providing {@link java.util.List<T>} of entityType class instances.
     *
     * @param entityType the target type class
     */
    public static <T> Translator<InputStream, List<T>> xmlList(Class<T> entityType) {
        return TranslatorHelper.inputStream2XmlObjectList(entityType);
    }

    /**
     * See {@link #xmlList(Class)}.
     */
    public static <T> Translator<InputStream, List<T>> xmlList(Class<T> entityType, ObjectMapper mapper) {
        return TranslatorHelper.inputStream2XmlObjectList(entityType, mapper);
    }

    /**
     * Expect XML data format from the source, will provide {@link com.fasterxml.jackson.core.JsonParser} instance
     * to the service factory
     */
    public static Translator<InputStream, JsonParser> xmlParser() {
        return TranslatorHelper.inputStream2XmlParser();
    }

    /**
     * See {@link #xmlParser()}.
     */
    public static Translator<InputStream, JsonParser> xmlParser(ObjectMapper mapper) {
        return TranslatorHelper.inputStream2XmlParser(mapper);
    }

    /**
     * Expect YAML data format from the source, will provide {@link com.fasterxml.jackson.databind.JsonNode} instance
     * to the service factory.
     */
    public static Translator<InputStream, JsonNode> yaml() {
        return TranslatorHelper.inputStream2Yaml();
    }

    /**
     * See {@link #yaml()}.
     */
    public static Translator<InputStream, JsonNode> yaml(ObjectMapper mapper) {
        return TranslatorHelper.inputStream2Yaml(mapper);
    }

    /**
     * Expect YAML data format from the source, will provide instance of entityType class to the service factory.
     *
     * @param entityType the target type class
     */
    public static <T> Translator<InputStream, T> yaml(Class<T> entityType) {
        return TranslatorHelper.inputStream2YamlObject(entityType);
    }

    /**
     * See {@link #yaml(Class)}.
     */
    public static <T> Translator<InputStream, T> yaml(Class<T> entityType, ObjectMapper mapper) {
        return TranslatorHelper.inputStream2YamlObject(entityType, mapper);
    }

    /**
     * Same as {@link #yaml(Class)}, but providing {@link java.util.List<T>} of entityType class instances.
     *
     * @param entityType the target type class
     */
    public static <T> Translator<InputStream, List<T>> yamlList(Class<T> entityType) {
        return TranslatorHelper.inputStream2YamlObjectList(entityType);
    }

    /**
     * See {@link #yamlList(Class)}.
     */
    public static <T> Translator<InputStream, List<T>> yamlList(Class<T> entityType, ObjectMapper mapper) {
        return TranslatorHelper.inputStream2YamlObjectList(entityType, mapper);
    }

    /**
     * Expect YAML data format from the source, will provide {@link com.fasterxml.jackson.core.JsonParser} instance
     * to the service factory.
     */
    public static Translator<InputStream, JsonParser> yamlParser() {
        return TranslatorHelper.inputStream2YamlParser();
    }

    /**
     * See {@link #yamlParser()}.
     */
    public static Translator<InputStream, JsonParser> yamlParser(ObjectMapper mapper) {
        return TranslatorHelper.inputStream2YamlParser(mapper);
    }

    /**
     * Expect JSON data format from the source, will provide {@link com.fasterxml.jackson.databind.JsonNode} instance
     * to the service factory.
     */
    public static Translator<InputStream, JsonNode> json() {
        return TranslatorHelper.inputStream2Json();
    }

    /**
     * See {@link #json()}.
     */
    public static Translator<InputStream, JsonNode> json(ObjectMapper mapper) {
        return TranslatorHelper.inputStream2Json(mapper);
    }

    /**
     * Expect JSON data format from the source, will provide instance of entityType class to the service factory.
     *
     * @param entityType the target type class
     */
    public static <T> Translator<InputStream, T> json(Class<T> entityType) {
        return TranslatorHelper.inputStream2JsonObject(entityType);
    }

    /**
     * See {@link #json(Class)}.
     */
    public static <T> Translator<InputStream, T> json(Class<T> entityType, ObjectMapper mapper) {
        return TranslatorHelper.inputStream2JsonObject(entityType, mapper);
    }

    /**
     * Same as {@link #json(Class)}, but providing {@link java.util.List<T>} of entityType class instances.
     *
     * @param entityType the target type class
     */
    public static <T> Translator<InputStream, List<T>> jsonList(Class<T> entityType) {
        return TranslatorHelper.inputStream2JsonObjectList(entityType);
    }

    /**
     * See {@link #jsonList(Class)}.
     */
    public static <T> Translator<InputStream, List<T>> jsonList(Class<T> entityType, ObjectMapper mapper) {
        return TranslatorHelper.inputStream2JsonObjectList(entityType, mapper);
    }

    /**
     * Expect JSON data format from the source, will provide {@link com.fasterxml.jackson.core.JsonParser} instance
     * to the service factory.
     */
    public static Translator<InputStream, JsonParser> jsonParser() {
        return TranslatorHelper.inputStream2JsonParser();
    }

    /**
     * See {@link #jsonParser()}.
     */
    public static Translator<InputStream, JsonParser> jsonParser(ObjectMapper mapper) {
        return TranslatorHelper.inputStream2JsonParser(mapper);
    }

    /**
     * Fetches the raw byte array from the source and pass it to the service factory as a byte[] array.
     */
    public static Translator<InputStream, byte[]> bytes() {
        return TranslatorHelper.inputStream2Bytes();
    }

    /**
     * Read the source data as a single string and pass it to the service factory as a String.
     */
    public static Translator<InputStream, String> string() {
        return TranslatorHelper.inputStream2String(UTF_8);
    }

    /**
     * Read the source data as a single string and pass it to the service factory as a String.
     *
     * @param charset the charset used to decode data
     */
    public static Translator<InputStream, String> string(Charset charset) {
        return TranslatorHelper.inputStream2String(charset);
    }

    /**
     * Read the source data as string lines (separated by System.lineSeparator) and pass it to the service factory
     * as a String[] array.
     */
    public static Translator<InputStream, String[]> lines() {
        return TranslatorHelper.inputStream2Lines(UTF_8);
    }

    /**
     * Read the source data as string lines (separated by {@link System#lineSeparator()}) and pass it to the service factory
     * as a String[] array.
     *
     * @param charset the charset used to decode data.
     */
    public static Translator<InputStream, String[]> lines(Charset charset) {
        return TranslatorHelper.inputStream2Lines(charset);
    }

}

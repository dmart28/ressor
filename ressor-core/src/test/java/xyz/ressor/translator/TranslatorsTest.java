package xyz.ressor.translator;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.databind.JsonNode;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.util.List;
import java.util.Random;
import java.util.zip.GZIPOutputStream;

import static java.nio.charset.StandardCharsets.ISO_8859_1;
import static java.nio.charset.StandardCharsets.UTF_8;
import static org.assertj.core.api.Assertions.assertThat;
import static xyz.ressor.commons.utils.FileUtils.classpath;
import static xyz.ressor.translator.Translator.define;
import static xyz.ressor.translator.Translators.*;

public class TranslatorsTest {

    @Test
    public void testBytes2String() {
        byte[] bytes = new byte[] { 97, 98, 99, 100, 101, 102 };
        assertThat(bytes2String().translate(bytes)).isEqualTo("abcdef");

        bytes = new byte[] { -47, -126, -48, -75, -47, -127, -47, -126 };
        assertThat(bytes2String().translate(bytes)).isEqualTo("тест");
        assertThat(bytes2String(ISO_8859_1).translate(bytes)).isNotEqualTo("тест");
    }

    @Test
    public void testInputStream2Bytes() {
        byte[] bytes = new byte[32 * 1024];
        new Random().nextBytes(bytes);
        ByteArrayInputStream is = new ByteArrayInputStream(bytes);

        assertThat(inputStream2Bytes().translate(is)).isEqualTo(bytes);
    }

    @Test
    public void testInputStream2GzippedBytes() throws IOException {
        byte[] bytes = new byte[32 * 1024];
        new Random().nextBytes(bytes);
        ByteArrayOutputStream os = new ByteArrayOutputStream();
        try (GZIPOutputStream gzip = new GZIPOutputStream(os)) {
            gzip.write(bytes);
        }

        assertThat(gzipped(inputStream2Bytes()).translate(new ByteArrayInputStream(os.toByteArray()))).isEqualTo(bytes);
    }

    @Test
    public void testInputStream2String() {
        byte[] bytes = new byte[] { 97, 98, 99, 100, 101, 102, 32, -47, -126, -48, -75, -47, -127, -47, -126 };
        ByteArrayInputStream is = new ByteArrayInputStream(bytes);

        assertThat(inputStream2String().translate(is)).isEqualTo("abcdef тест");
    }

    @Test
    public void testInputStream2Xml() {
        byte[] bytes = classpath("translator/event.xml").getBytes(UTF_8);
        JsonNode xml = inputStream2Xml().translate(new ByteArrayInputStream(bytes));

        assertThat(xml.has("to")).isTrue();
    }

    @Test
    public void testInputStream2Json() {
        byte[] bytes = classpath("translator/test.json").getBytes(UTF_8);
        JsonNode json = inputStream2Json().translate(new ByteArrayInputStream(bytes));

        assertThat(json.has("glossary")).isTrue();
    }

    @Test
    public void testInputStream2Yaml() {
        byte[] bytes = classpath("translator/event.yaml").getBytes(UTF_8);
        JsonNode yaml = inputStream2Yaml().translate(new ByteArrayInputStream(bytes));

        assertThat(yaml.get("invoice").asInt()).isEqualTo(34843);
        assertThat(yaml.get("comments").asText()).isEqualTo("Late afternoon is best. Backup contact is Nancy Billsmer @ 338-4338.");
    }

    @Test
    public void testInputStream2XmlParser() throws Exception {
        byte[] bytes = classpath("translator/event.xml").getBytes(UTF_8);
        JsonParser parser = inputStream2XmlParser().translate(new ByteArrayInputStream(bytes));

        assertThat(parser.nextToken()).isEqualTo(JsonToken.START_OBJECT);
        assertThat(parser.nextToken()).isEqualTo(JsonToken.FIELD_NAME);
        assertThat(parser.currentName()).isEqualTo("to");
        assertThat(parser.nextToken()).isEqualTo(JsonToken.VALUE_STRING);
    }

    @Test
    public void testInputStream2JsonParser() throws Exception {
        byte[] bytes = classpath("translator/test.json").getBytes(UTF_8);
        JsonParser parser = inputStream2JsonParser().translate(new ByteArrayInputStream(bytes));

        assertThat(parser.nextToken()).isEqualTo(JsonToken.START_OBJECT);
        assertThat(parser.nextToken()).isEqualTo(JsonToken.FIELD_NAME);
        assertThat(parser.currentName()).isEqualTo("glossary");
        assertThat(parser.nextToken()).isEqualTo(JsonToken.START_OBJECT);
    }

    @Test
    public void testInputStream2YamlParser() throws Exception {
        byte[] bytes = classpath("translator/event.yaml").getBytes(UTF_8);
        JsonParser parser = inputStream2YamlParser().translate(new ByteArrayInputStream(bytes));

        assertThat(parser.nextToken()).isEqualTo(JsonToken.START_OBJECT);
        assertThat(parser.nextToken()).isEqualTo(JsonToken.FIELD_NAME);
        assertThat(parser.currentName()).isEqualTo("invoice");
        assertThat(parser.nextToken()).isEqualTo(JsonToken.VALUE_NUMBER_INT);
        assertThat(parser.getIntValue()).isEqualTo(34843);
    }

    @Test
    public void testPrependingTranslator() {
        Translator<Integer, String> translator = define(i -> Integer.toString(i), Integer.class, String.class);
        Translator<byte[], String> nt = translator.prepend(bytes -> ByteBuffer.wrap(bytes).getInt(), byte[].class);

        assertThat(nt.translate(new byte[] { 0, 0, 5, 1 })).isEqualTo("1281");
    }

    @Test
    public void testXmlObjectTranslator() {
        byte[] bytes = classpath("translator/class_event.xml").getBytes(UTF_8);
        Translator<InputStream, Car> translator = Translators.inputStream2XmlObject(Car.class);

        translateAndCheckCar(bytes, translator);
    }

    @Test
    public void testJsonObjectTranslator() {
        byte[] bytes = classpath("translator/class_event.json").getBytes(UTF_8);
        Translator<InputStream, Car> translator = Translators.inputStream2JsonObject(Car.class);

        translateAndCheckCar(bytes, translator);
    }

    @Test
    public void testYamlObjectTranslator() {
        byte[] bytes = classpath("translator/class_event.yaml").getBytes(UTF_8);
        Translator<InputStream, Car> translator = Translators.inputStream2YamlObject(Car.class);

        translateAndCheckCar(bytes, translator);
    }

    @Test
    public void testXmlObjectListTranslator() {
        byte[] bytes = classpath("translator/class_events.xml").getBytes(UTF_8);
        List<Car> i = Translators.inputStream2XmlObjectList(Car.class).translate(new ByteArrayInputStream(bytes));

        assertThat(i).hasSize(1);
        checkCar(i.get(0));
    }

    @Test
    public void testJsonObjectListTranslator() {
        byte[] bytes = classpath("translator/class_events.json").getBytes(UTF_8);
        List<Car> i = Translators.inputStream2JsonObjectList(Car.class).translate(new ByteArrayInputStream(bytes));

        assertThat(i).hasSize(1);
        checkCar(i.get(0));
    }

    @Test
    public void testYamlObjectListTranslator() {
        byte[] bytes = classpath("translator/class_events.yaml").getBytes(UTF_8);
        List<Car> i = Translators.inputStream2YamlObjectList(Car.class).translate(new ByteArrayInputStream(bytes));

        assertThat(i).hasSize(1);
        checkCar(i.get(0));
    }

    private void translateAndCheckCar(byte[] bytes, Translator<InputStream, Car> translator) {
        checkCar(translator.translate(new ByteArrayInputStream(bytes)));
    }

    private void checkCar(Car car) {
        assertThat(car).isNotNull();
        assertThat(car.getModel()).isEqualTo("Model S");
        assertThat(car.getManufacturer()).isEqualTo("Tesla");
        assertThat(car.getWeight()).isEqualByComparingTo(4647d);
    }

}

package xyz.ressor.translator;

import com.fasterxml.jackson.core.JsonToken;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.nio.ByteBuffer;
import java.util.Random;
import java.util.function.Function;

import static java.nio.charset.StandardCharsets.*;
import static org.assertj.core.api.Assertions.assertThat;
import static xyz.ressor.commons.utils.FileUtils.classpath;
import static xyz.ressor.translator.Translators.*;

public class TranslatorsTest {

    @Test
    public void testBytes2String() {
        var bytes = new byte[] { 97, 98, 99, 100, 101, 102 };
        assertThat(bytes2String().translate(bytes)).isEqualTo("abcdef");

        bytes = new byte[] { -47, -126, -48, -75, -47, -127, -47, -126 };
        assertThat(bytes2String().translate(bytes)).isEqualTo("тест");
        assertThat(bytes2String(ISO_8859_1).translate(bytes)).isNotEqualTo("тест");
    }

    @Test
    public void testInputStream2Bytes() {
        var bytes = new byte[32 * 1024];
        new Random().nextBytes(bytes);
        var is = new ByteArrayInputStream(bytes);

        assertThat(inputStream2Bytes().translate(is)).isEqualTo(bytes);
    }

    @Test
    public void testInputStream2String() {
        var bytes = new byte[] { 97, 98, 99, 100, 101, 102, 32, -47, -126, -48, -75, -47, -127, -47, -126 };
        var is = new ByteArrayInputStream(bytes);

        assertThat(inputStream2String().translate(is)).isEqualTo("abcdef тест");
    }

    @Test
    public void testInputStream2Json() {
        var bytes = classpath("translator/test.json").getBytes(UTF_8);
        var is = new ByteArrayInputStream(bytes);

        var json = inputStream2Json().translate(is);
        assertThat(json.has("glossary")).isTrue();
    }

    @Test
    public void testInputStream2Yaml() {
        var bytes = classpath("translator/event.yaml").getBytes(UTF_8);
        var is = new ByteArrayInputStream(bytes);

        var yaml = inputStream2Yaml().translate(is);
        assertThat(yaml.get("invoice").asInt()).isEqualTo(34843);
        assertThat(yaml.get("comments").asText()).isEqualTo("Late afternoon is best. Backup contact is Nancy Billsmer @ 338-4338.");
    }

    @Test
    public void testInputStream2JsonParser() throws Exception {
        var bytes = classpath("translator/test.json").getBytes(UTF_8);
        var is = new ByteArrayInputStream(bytes);

        var parser = inputStream2JsonParser().translate(is);
        assertThat(parser.nextToken()).isEqualTo(JsonToken.START_OBJECT);
        assertThat(parser.nextToken()).isEqualTo(JsonToken.FIELD_NAME);
        assertThat(parser.currentName()).isEqualTo("glossary");
        assertThat(parser.nextToken()).isEqualTo(JsonToken.START_OBJECT);
    }

    @Test
    public void testInputStream2YamlParser() throws Exception {
        var bytes = classpath("translator/event.yaml").getBytes(UTF_8);
        var is = new ByteArrayInputStream(bytes);

        var parser = inputStream2YamlParser().translate(is);
        assertThat(parser.nextToken()).isEqualTo(JsonToken.START_OBJECT);
        assertThat(parser.nextToken()).isEqualTo(JsonToken.FIELD_NAME);
        assertThat(parser.currentName()).isEqualTo("invoice");
        assertThat(parser.nextToken()).isEqualTo(JsonToken.VALUE_NUMBER_INT);
        assertThat(parser.getIntValue()).isEqualTo(34843);
    }

    @Test
    public void testPrependingTranslator() {
        Translator<Integer, String> translator = i -> Integer.toString(i);

        var nt = translator.prepend((Function<byte[], Integer>) bytes -> ByteBuffer.wrap(bytes).getInt());

        assertThat(nt.translate(new byte[] { 0, 0, 5, 1 })).isEqualTo("1281");
    }

}

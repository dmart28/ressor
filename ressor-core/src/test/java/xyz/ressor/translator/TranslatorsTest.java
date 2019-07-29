package xyz.ressor.translator;

import org.junit.jupiter.api.Test;

import java.nio.charset.StandardCharsets;
import java.util.Arrays;

import static java.nio.charset.StandardCharsets.UTF_8;
import static org.assertj.core.api.Assertions.assertThat;

public class TranslatorsTest {

    @Test
    public void testBytes2String() {
        var bytes = new byte[] { 97, 98, 99, 100, 101, 102 };
        assertThat(Translators.bytes2String().translate(bytes)).isEqualTo("abcdef");

        bytes = new byte[] { -47, -126, -48, -75, -47, -127, -47, -126 };
        assertThat(Translators.bytes2String().translate(bytes)).isEqualTo("тест");
        assertThat(Translators.bytes2String(StandardCharsets.ISO_8859_1).translate(bytes)).isNotEqualTo("тест");
    }



}

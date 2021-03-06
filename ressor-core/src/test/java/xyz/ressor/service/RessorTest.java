package xyz.ressor.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import xyz.ressor.Ressor;

import java.util.concurrent.TimeUnit;
import java.util.function.Function;

import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;
import static xyz.ressor.translator.Translators.string;
import static xyz.ressor.utils.TestUtils.stringBuilderSource;

@TestInstance(TestInstance.Lifecycle.PER_METHOD)
public class RessorTest {
    private Ressor ressor = Ressor.create();

    @Test
    public void testManualReload() {
        var sb = new StringBuilder();
        var string = stringBuilderSource(sb, ressor.service(CharSequence.class)
                .translator(string())
                .factory(Function.identity())).build();

        sb.append("reload");
        assertThat(string).isEqualTo("");

        ressor.reload(string);

        sb.append(" async");
        assertThat(string).isEqualTo("reload");

        ressor.scheduleReload(string);

        await().atMost(10, TimeUnit.SECONDS).until(() -> string.equals("reload async"));
    }

}

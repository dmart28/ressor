package xyz.ressor.service.proxy;

import org.junit.jupiter.api.Test;
import xyz.ressor.service.RessorService;
import xyz.ressor.translator.Translators;

import java.util.function.Function;

import static org.assertj.core.api.Assertions.assertThat;

public class ServiceProxyBuilderTest {

    @Test
    public void testSimpleProxyCase() {
        var b = new ServiceProxyBuilder();
        StringTestClass1 instance = b.buildProxy(ProxyContext.builder(StringTestClass1.class)
                .translator(Translators.inputStream2String())
                .factory(StringTestClass1::new)
                .build());

        assertThat(instance).isInstanceOf(RessorService.class);
    }

    public static class StringTestClass1 {
        public final String val;

        public StringTestClass1(String val) {
            this.val = val;
        }
    }

}

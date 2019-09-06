package xyz.ressor.service.proxy;

import org.junit.jupiter.api.Test;
import xyz.ressor.commons.annotations.ProxyConstructor;
import xyz.ressor.service.RessorService;
import xyz.ressor.translator.Translators;

import java.util.function.Function;

import static org.assertj.core.api.Assertions.assertThat;
import static xyz.ressor.utils.TestUtils.illegalConstructor;

public class ServiceProxyBuilderTest {

    @Test
    public void testAllConstructorsPrivate() {
        AllPrivateConstructors instance = new ServiceProxyBuilder()
                .buildProxy(ProxyContext.builder(AllPrivateConstructors.class)
                .translator(Translators.inputStream2String())
                .factory((Function<String, AllPrivateConstructors>) AllPrivateConstructors::new)
                .build());

        assertThat(instance).isInstanceOf(RessorService.class);
        assertThat(instance.val).isNull();

    }

    @Test
    public void testSimpleProxyCase() {

    }

    public static class AllPrivateConstructors {
        public final String val;

        public String getVal() {
            return val;
        }

        private AllPrivateConstructors(String val) {
            this.val = val;
        }

        private AllPrivateConstructors(String val, Object o) {
            throw illegalConstructor();
        }

        private AllPrivateConstructors(String val, int a) {
            throw illegalConstructor();
        }

        private AllPrivateConstructors(String val, int b, int c) {
            throw illegalConstructor();
        }

        @ProxyConstructor
        public static AllPrivateConstructors create(String v) {
            return new AllPrivateConstructors(v);
        }

    }

}

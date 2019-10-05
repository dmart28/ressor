package xyz.ressor.ext.spring;

import net.bytebuddy.ByteBuddy;
import net.bytebuddy.dynamic.DynamicType;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.FactoryBean;
import xyz.ressor.service.RessorService;
import xyz.ressor.source.LoadedResource;
import xyz.ressor.source.SourceVersion;

import static net.bytebuddy.dynamic.loading.ClassLoadingStrategy.Default.INJECTION;
import static org.assertj.core.api.Assertions.assertThat;

public class FactoryBeanExtensionTest {

    @Test
    public void test() throws Exception {
        var ext = new FactoryBeanExtension();
        var bb = new ByteBuddy();
        DynamicType.Builder<TestRessorService> b = bb.subclass(TestRessorService.class);

        var instance = ext.interceptProxy(b, TestRessorService.class)
                .make()
                .load(getClass().getClassLoader(), INJECTION)
                .getLoaded()
                .getDeclaredConstructor()
                .newInstance();

        assertThat(instance).isInstanceOf(FactoryBean.class);
        assertThat(((FactoryBean) instance).getObject()).isSameAs(TestRessorService.INSTANCE);
        assertThat(((FactoryBean) instance).getObjectType()).isSameAs(TestRessorService.class);
    }

    public static class TestRessorService implements RessorService<Object> {
        public static final Object INSTANCE = new Object();

        @Override
        public RessorService<Object> unwrap() {
            return this;
        }

        @Override
        public Class<? extends Object> underlyingType() {
            return TestRessorService.class;
        }

        @Override
        public Object instance() {
            return INSTANCE;
        }

        @Override
        public SourceVersion currentVersion() {
            return null;
        }

        @Override
        public void reload(LoadedResource resource) {
        }
    }

}

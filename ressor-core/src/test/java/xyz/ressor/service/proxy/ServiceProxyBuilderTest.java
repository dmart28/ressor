package xyz.ressor.service.proxy;

import org.junit.jupiter.api.Test;
import xyz.ressor.commons.exceptions.TypeDefinitionException;
import xyz.ressor.service.proxy.model.JsonCarRepository;
import xyz.ressor.service.proxy.model.VeryBasicClass;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static xyz.ressor.translator.Translators.inputStream2Json;

public class ServiceProxyBuilderTest {
    private ServiceProxyBuilder proxyBuilder = new ServiceProxyBuilder();

    @Test
    public void testVeryBasicClass() {
        assertThrows(TypeDefinitionException.class, () -> proxyBuilder.buildProxy(
                ProxyContext.builder(VeryBasicClass.class).build()));
    }

    @Test
    public void testJsonCarRepository() {
        var initialInstance = new JsonCarRepository("-", "-");
        var carRepository = proxyBuilder.buildProxy(ProxyContext
                .builder(JsonCarRepository.class)
                .translator(inputStream2Json())
                .initialInstance(initialInstance)
                .build());

        assertThat(carRepository).isNotNull();
        assertThat(carRepository).isNotSameAs(initialInstance);
        assertThat(carRepository.getModel()).isEqualTo("-");
        assertThat(carRepository.getManufacturer()).isEqualTo("-");
    }

}

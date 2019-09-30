package xyz.ressor.service.proxy;

import org.junit.jupiter.api.Test;
import xyz.ressor.commons.exceptions.TypeDefinitionException;
import xyz.ressor.service.RessorService;
import xyz.ressor.service.proxy.model.JsonCarRepository;
import xyz.ressor.service.proxy.model.JsonConstructorOnlyCarRepository;
import xyz.ressor.service.proxy.model.JsonNestedCarRepository;
import xyz.ressor.service.proxy.model.VeryBasicClass;
import xyz.ressor.source.LoadedResource;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static xyz.ressor.translator.Translators.inputStream2Json;
import static xyz.ressor.utils.TestUtils.load;

public class ServiceProxyBuilderTest {
    private ServiceProxyBuilder proxyBuilder = new ServiceProxyBuilder();

    @Test
    public void testVeryBasicClass() {
        assertThrows(TypeDefinitionException.class, () -> proxyBuilder.buildProxy(
                ProxyContext.builder(VeryBasicClass.class).translator(inputStream2Json()).build()));
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

        ((RessorService<JsonCarRepository>) carRepository).reload(load("classpath:proxy/car_repository.json"));

        assertThat(carRepository.getModel()).isEqualTo("Astra");
        assertThat(carRepository.getManufacturer()).isEqualTo("Opel");
    }

    @Test
    public void testNestedJsonCarRepository() {
        var nestedCarRepository = proxyBuilder.buildProxy(ProxyContext
                .builder(JsonNestedCarRepository.class)
                .translator(inputStream2Json()).build());

        assertThat(nestedCarRepository).isNotNull();

        ((RessorService<JsonNestedCarRepository>) nestedCarRepository).reload(load("classpath:proxy/nested_car_repository.json"));

        assertThat(nestedCarRepository.getModel()).isEqualTo("Scirocco");
        assertThat(nestedCarRepository.getManufacturer()).isEqualTo("Volkswagen");
        assertThat(nestedCarRepository.getClearance()).isEqualTo(130d);

        assertThat(nestedCarRepository.finalMethod()).isEqualTo(nestedCarRepository.getManufacturer());
        assertThat(nestedCarRepository.computeClearance(2)).isEqualTo(260d);

        ((RessorService<JsonNestedCarRepository>) nestedCarRepository).reload(load("classpath:proxy/car_repository.json"));

        assertThat(nestedCarRepository.getModel()).isEqualTo("Astra");
        assertThat(nestedCarRepository.getManufacturer()).isEqualTo("Opel");
        assertThat(nestedCarRepository.getClearance()).isEqualTo(0d);
    }

    @Test
    public void testConstructorOnlyCarRepository() {
        var carRepository = proxyBuilder.buildProxy(ProxyContext
                .builder(JsonConstructorOnlyCarRepository.class)
                .translator(inputStream2Json()).build());

        assertThat(carRepository).isNotNull();

        ((RessorService<JsonConstructorOnlyCarRepository>) carRepository).reload(load("classpath:proxy/car_repository.json"));

        assertThat(carRepository.getModel()).isEqualTo("Astra");
        assertThat(carRepository.getManufacturer()).isEqualTo("Opel");
    }

}

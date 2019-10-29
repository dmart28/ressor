package xyz.ressor.service.proxy;

import com.fasterxml.jackson.databind.JsonNode;
import org.junit.jupiter.api.Test;
import xyz.ressor.commons.exceptions.TypeDefinitionException;
import xyz.ressor.service.RessorService;
import xyz.ressor.service.proxy.model.*;
import xyz.ressor.translator.Translator;

import java.io.InputStream;
import java.lang.reflect.InvocationTargetException;
import java.util.function.Function;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static xyz.ressor.translator.Translators.inputStream2Json;
import static xyz.ressor.utils.TestUtils.json;
import static xyz.ressor.utils.TestUtils.load;

public class ServiceProxyBuilderTest {
    private ServiceProxyBuilder proxyBuilder = new ServiceProxyBuilder(true);

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

        ((RessorService<JsonNestedCarRepository>) nestedCarRepository).reload(load("classpath:proxy/car_repository.json", true));

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

    @Test
    public void testDefaultArgumentsJsonCarRepository() {
        var carRepository = proxyBuilder.buildProxy(ProxyContext
                .builder(DefaultArgumentsJsonCarRepository.class)
                .translator(inputStream2Json())
                .proxyDefaultArguments(json("{\"model\":\"None\",\"manufacturer\":\"None\"}"))
                .initialInstance(new DefaultArgumentsJsonCarRepository(json("{\"model\":\"-\",\"manufacturer\":\"-\"}")))
                .build());

        assertThat(carRepository).isNotNull();
        assertThat(carRepository.getModel()).isEqualTo("-");
        assertThat(carRepository.getManufacturer()).isEqualTo("-");
    }

    @Test
    public void testNoDefaultArgumentsJsonCarRepository() {
        assertThrows(InvocationTargetException.class, () -> proxyBuilder.buildProxy(ProxyContext
                .builder(DefaultArgumentsJsonCarRepository.class)
                .translator(inputStream2Json())
                .initialInstance(new DefaultArgumentsJsonCarRepository(json("{\"model\":\"-\",\"manufacturer\":\"-\"}")))
                .build()));
    }

    @Test
    public void testInterfaceRepository() {
        var personInfo = proxyBuilder.buildProxy(ProxyContext
                .builder(PersonInfo.class)
                .translator(inputStream2Json())
                .factory((JsonNode n) -> new PersonInfoImpl(n.path("first_name").asText(), n.path("last_name").asText()))
                .build());

        assertThat(personInfo).isNotNull();

        ((RessorService<PersonInfo>) personInfo).reload(load("classpath:proxy/person_info.json"));

        assertThat(personInfo.firstName()).isEqualTo("John");
        assertThat(personInfo.lastName()).isEqualTo("Doe");
        assertThat(personInfo).isNotInstanceOf(PersonInfoImpl.class);
    }

    @Test
    public void testProxyClassCaching() {
        Function<ServiceProxyBuilder, PersonInfo> f = pb -> pb.buildProxy(ProxyContext.builder(PersonInfo.class)
                .translator(inputStream2Json())
                .factory(n -> new PersonInfoImpl(null, null)).build());

        var p1 = f.apply(proxyBuilder);
        var p2 = f.apply(proxyBuilder);

        assertThat(p1).isNotEqualTo(p2);
        assertThat(p1.getClass()).isSameAs(p2.getClass());

        var noCacheProxyBuilder = new ServiceProxyBuilder(false);

        p1 = f.apply(noCacheProxyBuilder);
        p2 = f.apply(noCacheProxyBuilder);

        assertThat(p1).isNotEqualTo(p2);
        assertThat(p1.getClass()).isNotSameAs(p2.getClass());
    }

    @Test
    public void testProxyClassCachingConditions() {
        Function<Object[], PublicClassConstructorAnnotated> f = dpa -> proxyBuilder.buildProxy(ProxyContext.builder(PublicClassConstructorAnnotated.class)
                .translator(Translator.define(s -> 5, InputStream.class, int.class))
                .proxyDefaultArguments(dpa)
                .build());

        var p1 = f.apply(new Object[] { 0, 0L });
        var p2 = f.apply(new Object[] { 0, 0L });

        assertThat(p1).isNotEqualTo(p2);
        assertThat(p1.getClass()).isSameAs(p2.getClass());

        p1 = f.apply(new Object[] { 0, 0L });
        p2 = f.apply(new Object[] { 1, 1L });

        assertThat(p1).isNotEqualTo(p2);
        assertThat(p1.getClass()).isNotSameAs(p2.getClass());
    }

}

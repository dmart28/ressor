package xyz.ressor.integration;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.MissingNode;
import org.junit.jupiter.api.Test;
import xyz.ressor.Ressor;
import xyz.ressor.integration.model.geo.GeoService;
import xyz.ressor.integration.model.geo.GeoServiceImpl;

import static org.assertj.core.api.Assertions.assertThat;

public class GeoServiceTest {

    @Test
    public void testJson() {
        checkGeoService(Ressor.builder(GeoService.class)
                .fileSource("classpath:integration/geoData.json")
                .json()
                .<JsonNode>factory(GeoServiceImpl::new)
                .build());
    }

    @Test
    public void testYaml() {
        checkGeoService(Ressor.builder(GeoService.class)
                .fileSource("classpath:integration/geoData.yml")
                .yaml()
                .<JsonNode>factory(GeoServiceImpl::new)
                .build());
    }

    @Test
    public void testString() {
        checkGeoService(Ressor.builder(GeoService.class)
                .fileSource("classpath:integration/geoData.csv")
                .string()
                .<String>factory(s -> new GeoServiceImpl(s.split(System.lineSeparator())))
                .build());
    }

    @Test
    public void testLines() {
        checkGeoService(Ressor.builder(GeoService.class)
                .fileSource("classpath:integration/geoData.csv")
                .lines()
                .<String[]>factory(GeoServiceImpl::new)
                .build());
    }

    @Test
    public void testJsonImplementationOnly() {
        checkGeoService(Ressor.builder(GeoServiceImpl.class)
                .fileSource("classpath:integration/geoData.json")
                .json()
                .proxyDefaultArguments(MissingNode.getInstance())
                .build());
    }

    @Test
    public void testYamlImplementationOnly() {
        checkGeoService(Ressor.builder(GeoServiceImpl.class)
                .fileSource("classpath:integration/geoData.yml")
                .yaml()
                .proxyDefaultArguments(MissingNode.getInstance())
                .build());
    }

    @Test
    public void testLinesImplementationOnly() {
        checkGeoService(Ressor.builder(GeoServiceImpl.class)
                .fileSource("classpath:integration/geoData.csv")
                .lines()
                .proxyDefaultArguments(MissingNode.getInstance())
                .build());
    }

    private void checkGeoService(GeoService geoService) {
        assertThat(geoService).isNotNull();
        var ipInfo = geoService.detect("92.154.89.56");
        assertThat(ipInfo).isNotNull();
        assertThat(ipInfo.getCountryCode()).isEqualTo("FR");
        assertThat(ipInfo.getLatitude()).isEqualTo(48.8543);
        assertThat(ipInfo.getLongitude()).isEqualTo(2.3527);

        ipInfo = geoService.detect("193.124.4.85");
        assertThat(ipInfo).isNotNull();
        assertThat(ipInfo.getCountryCode()).isEqualTo("RU");
        assertThat(ipInfo.getLatitude()).isEqualTo(55.7522);
        assertThat(ipInfo.getLongitude()).isEqualTo(37.6156);

        ipInfo = geoService.detect("145.123.3.5");
        assertThat(ipInfo).isNotNull();
        assertThat(ipInfo.getCountryCode()).isEqualTo("NL");
        assertThat(ipInfo.getLatitude()).isEqualTo(52.3824);
        assertThat(ipInfo.getLongitude()).isEqualTo(4.8995);

        assertThat(geoService.detect("1.1.1.1")).isNull();
    }

}

package xyz.ressor.integration;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.MissingNode;
import org.apache.commons.io.IOUtils;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import xyz.ressor.Ressor;
import xyz.ressor.integration.model.geo.GeoData;
import xyz.ressor.integration.model.geo.GeoService;
import xyz.ressor.integration.model.geo.GeoServiceImpl;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.List;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;

public class GeoServiceTest {

    @Test
    public void testXml() {
        checkGeoService(Ressor.service(GeoService.class)
                .fileSource("classpath:integration/geoData.xml")
                .xml()
                .<JsonNode>factory(GeoServiceImpl::new)
                .build());
    }

    @Test
    public void testXmlObject() {
        checkGeoService(Ressor.service(GeoService.class)
                .fileSource("classpath:integration/geoData.xml")
                .xmlList(GeoData.class)
                .<List<GeoData>>factory(GeoServiceImpl::new)
                .build());
    }

    @Test
    public void testJson() {
        checkGeoService(Ressor.service(GeoService.class)
                .fileSource("classpath:integration/geoData.json")
                .json()
                .<JsonNode>factory(GeoServiceImpl::new)
                .build());
    }

    @Test
    public void testGzipJson() {
        checkGeoService(Ressor.service(GeoService.class)
                .fileSource("classpath:integration/geoData.json.gz")
                .json()
                .gzipped()
                .<JsonNode>factory(GeoServiceImpl::new)
                .build());
    }

    @Test
    public void testYaml() {
        checkGeoService(Ressor.service(GeoService.class)
                .fileSource("classpath:integration/geoData.yml")
                .yaml()
                .<JsonNode>factory(GeoServiceImpl::new)
                .build());
    }

    @Test
    public void testYamlObject() {
        checkGeoService(Ressor.service(GeoService.class)
                .fileSource("classpath:integration/geoData.yml")
                .yamlList(GeoData.class)
                .<List<GeoData>>factory(GeoServiceImpl::new)
                .build());
    }

    @Test
    public void testJsonObject() {
        checkGeoService(Ressor.service(GeoService.class)
                .fileSource("classpath:integration/geoData.json")
                .jsonList(GeoData.class)
                .<List<GeoData>>factory(GeoServiceImpl::new)
                .build());
    }

    @Test
    public void testString() {
        checkGeoService(Ressor.service(GeoService.class)
                .fileSource("classpath:integration/geoData.csv")
                .string()
                .<String>factory(s -> new GeoServiceImpl(s.split(System.lineSeparator())))
                .build());
    }

    @Test
    public void testLines() {
        checkGeoService(Ressor.service(GeoService.class)
                .fileSource("classpath:integration/geoData.csv")
                .lines()
                .<String[]>factory(GeoServiceImpl::new)
                .build());
    }

    @Test
    public void testJsonImplementationOnly() {
        checkGeoService(Ressor.service(GeoServiceImpl.class)
                .fileSource("classpath:integration/geoData.json")
                .json()
                .proxyDefaultArguments(MissingNode.getInstance())
                .build());
    }

    @Test
    public void testJsonObjectImplementationOnly() {
        checkGeoService(Ressor.service(GeoServiceImpl.class)
                .fileSource("classpath:integration/geoData.json")
                .jsonList(GeoData.class)
                .proxyDefaultArguments(MissingNode.getInstance())
                .build());
    }

    @Test
    public void testYamlObjectImplementationOnly() {
        checkGeoService(Ressor.service(GeoServiceImpl.class)
                .fileSource("classpath:integration/geoData.yml")
                .yamlList(GeoData.class)
                .proxyDefaultArguments(MissingNode.getInstance())
                .build());
    }

    @Test
    public void testYamlImplementationOnly() {
        checkGeoService(Ressor.service(GeoServiceImpl.class)
                .fileSource("classpath:integration/geoData.yml")
                .yaml()
                .proxyDefaultArguments(MissingNode.getInstance())
                .build());
    }

    @Test
    public void testLinesImplementationOnly() {
        checkGeoService(Ressor.service(GeoServiceImpl.class)
                .fileSource("classpath:integration/geoData.csv")
                .lines()
                .proxyDefaultArguments(MissingNode.getInstance())
                .build());
    }

    @Test
    public void testListenLoader(@TempDir Path tempDir) throws Exception {
        IOUtils.copy(getClass().getClassLoader().getResourceAsStream("integration/geoData.yml"),
                Files.newOutputStream(tempDir.resolve("file.yml"), StandardOpenOption.CREATE));
        var geoService = Ressor.service(GeoService.class)
                .fileSource(tempDir.resolve("file.yml"))
                .yaml()
                .<JsonNode>factory(GeoServiceImpl::new)
                .build();

        Ressor.listen(geoService);

        assertThat(geoService.detect("193.124.4.85").getCountryCode()).isEqualTo("RU");

        IOUtils.copy(getClass().getClassLoader().getResourceAsStream("integration/newGeoData.yml"),
                Files.newOutputStream(tempDir.resolve("file.yml"), StandardOpenOption.TRUNCATE_EXISTING));

        await().atMost(30, TimeUnit.SECONDS)
                .pollInterval(100, TimeUnit.MILLISECONDS)
                .until(() -> geoService.detect("193.124.4.85").getCountryCode().equals("CN"));

        Ressor.stop(geoService);

        IOUtils.copy(getClass().getClassLoader().getResourceAsStream("integration/geoData.yml"),
                Files.newOutputStream(tempDir.resolve("file.yml"), StandardOpenOption.TRUNCATE_EXISTING));

        Ressor.poll(geoService).every(2, TimeUnit.SECONDS);

        await().atMost(10, TimeUnit.SECONDS)
                .pollInterval(1, TimeUnit.SECONDS)
                .until(() -> geoService.detect("193.124.4.85").getCountryCode().equals("RU"));

        Ressor.poll(geoService).cron("* * * * * ? *");

        IOUtils.copy(getClass().getClassLoader().getResourceAsStream("integration/newGeoData.yml"),
                Files.newOutputStream(tempDir.resolve("file.yml"), StandardOpenOption.TRUNCATE_EXISTING));

        await().atMost(10, TimeUnit.SECONDS)
                .pollInterval(1, TimeUnit.SECONDS)
                .until(() -> geoService.detect("193.124.4.85").getCountryCode().equals("CN"));
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

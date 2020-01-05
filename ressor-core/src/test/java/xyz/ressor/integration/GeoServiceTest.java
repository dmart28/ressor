package xyz.ressor.integration;

import com.fasterxml.jackson.databind.node.MissingNode;
import org.apache.commons.io.IOUtils;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import xyz.ressor.Ressor;
import xyz.ressor.commons.exceptions.RessorBuilderException;
import xyz.ressor.config.RessorConfig;
import xyz.ressor.integration.model.geo.GeoData;
import xyz.ressor.integration.model.geo.GeoService;
import xyz.ressor.integration.model.geo.GeoServiceImpl;
import xyz.ressor.source.ResourceId;
import xyz.ressor.source.fs.FileSystemSource;

import java.io.FileNotFoundException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.BiConsumer;

import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static xyz.ressor.translator.Translators.json;
import static xyz.ressor.translator.Translators.jsonList;
import static xyz.ressor.translator.Translators.lines;
import static xyz.ressor.translator.Translators.string;
import static xyz.ressor.translator.Translators.xml;
import static xyz.ressor.translator.Translators.xmlList;
import static xyz.ressor.translator.Translators.yaml;
import static xyz.ressor.translator.Translators.yamlList;
import static xyz.ressor.utils.TestUtils.simpleErrorHandler;

public class GeoServiceTest {

    @Test
    public void testXml() {
        checkGeoService(Ressor.create().service(GeoService.class)
                .fileSource("classpath:integration/geoData.xml")
                .translator(xml())
                .factory(GeoServiceImpl::new)
                .build());
    }

    @Test
    public void testXmlObject() {
        checkGeoService(Ressor.create().service(GeoService.class)
                .fileSource("classpath:integration/geoData.xml")
                .translator(xmlList(GeoData.class))
                .factory(GeoServiceImpl::new)
                .build());
    }

    @Test
    public void testJson() {
        checkGeoService(Ressor.create().service(GeoService.class)
                .fileSource("classpath:integration/geoData.json")
                .translator(json())
                .factory(GeoServiceImpl::new)
                .build());
    }

    @Test
    public void testGzipJson() {
        checkGeoService(Ressor.create().service(GeoService.class)
                .fileSource("classpath:integration/geoData.json.gz")
                .translator(json())
                .gzipped()
                .factory(GeoServiceImpl::new)
                .build());
    }

    @Test
    public void testYaml() {
        checkGeoService(Ressor.create().service(GeoService.class)
                .fileSource("classpath:integration/geoData.yml")
                .translator(yaml())
                .factory(GeoServiceImpl::new)
                .build());
    }

    @Test
    public void testYamlObject() {
        checkGeoService(Ressor.create().service(GeoService.class)
                .fileSource("classpath:integration/geoData.yml")
                .translator(yamlList(GeoData.class))
                .factory(GeoServiceImpl::new)
                .build());
    }

    @Test
    public void testJsonObject() {
        checkGeoService(Ressor.create().service(GeoService.class)
                .fileSource("classpath:integration/geoData.json")
                .translator(jsonList(GeoData.class))
                .factory(GeoServiceImpl::new)
                .build());
    }

    @Test
    public void testString() {
        checkGeoService(Ressor.create().service(GeoService.class)
                .fileSource("classpath:integration/geoData.csv")
                .translator(string())
                .factory(s -> new GeoServiceImpl(s.split(System.lineSeparator())))
                .build());
    }

    @Test
    public void testLines() {
        checkGeoService(Ressor.create().service(GeoService.class)
                .fileSource("classpath:integration/geoData.csv")
                .translator(lines())
                .factory(GeoServiceImpl::new)
                .build());
    }

    @Test
    public void testJsonImplementationOnly() {
        checkGeoService(Ressor.create().service(GeoServiceImpl.class)
                .fileSource("classpath:integration/geoData.json")
                .translator(json())
                .proxyDefaultArguments(MissingNode.getInstance())
                .build());
    }

    @Test
    public void testJsonObjectImplementationOnly() {
        checkGeoService(Ressor.create().service(GeoServiceImpl.class)
                .fileSource("classpath:integration/geoData.json")
                .translator(jsonList(GeoData.class))
                .proxyDefaultArguments(MissingNode.getInstance())
                .build());
    }

    @Test
    public void testYamlObjectImplementationOnly() {
        checkGeoService(Ressor.create().service(GeoServiceImpl.class)
                .fileSource("classpath:integration/geoData.yml")
                .translator(yamlList(GeoData.class))
                .proxyDefaultArguments(MissingNode.getInstance())
                .build());
    }

    @Test
    public void testYamlImplementationOnly() {
        checkGeoService(Ressor.create().service(GeoServiceImpl.class)
                .fileSource("classpath:integration/geoData.yml")
                .translator(yaml())
                .proxyDefaultArguments(MissingNode.getInstance())
                .build());
    }

    @Test
    public void testLinesImplementationOnly() {
        checkGeoService(Ressor.create().service(GeoServiceImpl.class)
                .fileSource("classpath:integration/geoData.csv")
                .translator(lines())
                .proxyDefaultArguments(MissingNode.getInstance())
                .build());
    }

    @Test
    public void testFileNotExists() {
        assertThrows(FileNotFoundException.class, () -> Ressor.create().service(GeoServiceImpl.class)
                .fileSource("classpath:integration/fileNotExists.csv")
                .translator(lines())
                .proxyDefaultArguments(MissingNode.getInstance())
                .build());
    }

    @Test
    public void testListenLoader(@TempDir Path tempDir) throws Exception {
        IOUtils.copy(getClass().getClassLoader().getResourceAsStream("integration/geoData.yml"),
                Files.newOutputStream(tempDir.resolve("file.yml"), StandardOpenOption.CREATE));
        var ressor = Ressor.create();
        var geoService = ressor.service(GeoService.class)
                .fileSource(tempDir.resolve("file.yml"))
                .translator(yaml())
                .factory(GeoServiceImpl::new)
                .build();

        ressor.listen(geoService);

        assertThat(geoService.detect("193.124.4.85").getCountryCode()).isEqualTo("RU");

        IOUtils.copy(getClass().getClassLoader().getResourceAsStream("integration/newGeoData.yml"),
                Files.newOutputStream(tempDir.resolve("file.yml"), StandardOpenOption.TRUNCATE_EXISTING));

        await().atMost(30, TimeUnit.SECONDS)
                .pollInterval(100, TimeUnit.MILLISECONDS)
                .until(() -> geoService.detect("193.124.4.85").getCountryCode().equals("CN"));

        ressor.stop(geoService);

        IOUtils.copy(getClass().getClassLoader().getResourceAsStream("integration/geoData.yml"),
                Files.newOutputStream(tempDir.resolve("file.yml"), StandardOpenOption.TRUNCATE_EXISTING));

        ressor.poll(geoService).every(2, TimeUnit.SECONDS);

        await().atMost(10, TimeUnit.SECONDS)
                .pollInterval(1, TimeUnit.SECONDS)
                .until(() -> geoService.detect("193.124.4.85").getCountryCode().equals("RU"));

        ressor.poll(geoService).cron("* * * * * ? *");

        IOUtils.copy(getClass().getClassLoader().getResourceAsStream("integration/newGeoData.yml"),
                Files.newOutputStream(tempDir.resolve("file.yml"), StandardOpenOption.TRUNCATE_EXISTING));

        await().atMost(10, TimeUnit.SECONDS)
                .pollInterval(1, TimeUnit.SECONDS)
                .until(() -> geoService.detect("193.124.4.85").getCountryCode().equals("CN"));
    }

    @Test
    public void testServiceErrorHandler() {
        var reloadFailed = new AtomicInteger();
        var translateFailed = new AtomicInteger();

        var errorHandler = simpleErrorHandler(reloadFailed::incrementAndGet, translateFailed::incrementAndGet);
        BiConsumer<String, RessorConfig> createService = (path, config) -> Ressor.create(config).service(GeoService.class)
                .fileSource(path)
                .translator(json())
                .factory(GeoServiceImpl::new)
                .errorHandler(errorHandler)
                .build();

        createService.accept("classpath:integration/fileNotExist.json", new RessorConfig());
        assertThat(reloadFailed.getAndSet(0)).isOne();
        assertThat(translateFailed.getAndSet(0)).isZero();

        createService.accept("classpath:integration/geoDataBroken.json", new RessorConfig());
        assertThat(reloadFailed.getAndSet(0)).isZero();
        assertThat(translateFailed.getAndSet(0)).isOne();

        var globalConfig = new RessorConfig().errorHandler(errorHandler);

        createService.accept("classpath:integration/fileNotExist.json", globalConfig);
        assertThat(reloadFailed.getAndSet(0)).isOne();
        assertThat(translateFailed.getAndSet(0)).isZero();

        createService.accept("classpath:integration/geoDataBroken.json", globalConfig);
        assertThat(reloadFailed.getAndSet(0)).isZero();
        assertThat(translateFailed.getAndSet(0)).isOne();
    }

    @Test
    public void testResourceRelatedErrors() {
        assertThrows(RessorBuilderException.class, () -> Ressor.create().service(GeoService.class)
                .source(new FileSystemSource())
                .translator(xml()).factory(GeoServiceImpl::new).build());

        assertThrows(RessorBuilderException.class, () -> Ressor.create().service(GeoService.class)
                .source(new FileSystemSource())
                .resource(new TestResourceId())
                .translator(xml()).factory(GeoServiceImpl::new).build());

        assertThrows(RessorBuilderException.class, () -> Ressor.create().service(GeoService.class)
                .resource(new TestResourceId())
                .translator(xml()).factory(GeoServiceImpl::new).build());
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

    private static class TestResourceId implements ResourceId {

        @Override
        public Class<?> sourceType() {
            return String.class;
        }
    }

}

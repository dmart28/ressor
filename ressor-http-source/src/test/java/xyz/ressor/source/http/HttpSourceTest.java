package xyz.ressor.source.http;

import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.client.MappingBuilder;
import com.github.tomakehurst.wiremock.core.Options;
import org.apache.commons.io.IOUtils;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import xyz.ressor.source.version.LastModified;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static java.nio.charset.StandardCharsets.UTF_8;
import static org.assertj.core.api.Assertions.assertThat;

public class HttpSourceTest {
    public static final String PATH = "/resource";
    private WireMockServer wireMockServer;

    @BeforeEach
    public void setUp() {
        wireMockServer = new WireMockServer(Options.DYNAMIC_PORT);
        wireMockServer.start();
        configureFor(wireMockServer.port());
    }

    @AfterEach
    public void tearDown() {
        wireMockServer.stop();
    }

    @Test
    public void testNoCacheSupportedScenario() throws Exception {
        stubFor(path()
                .willReturn(aResponse()
                        .withStatus(200).withBody("one")));

        var source = new HttpSource(client(), defaultURL(), CacheControlStrategy.NONE);
        assertThat(IOUtils.toString(source.load().getInputStream(), UTF_8)).isEqualTo("one");
        assertThat(IOUtils.toString(source.loadIfModified(new LastModified(System.currentTimeMillis()))
                .getInputStream(), UTF_8)).isEqualTo("one");
    }

    @Test
    public void testIfModifiedSinceScenario() throws Exception {
        stubFor(path().willReturn(aResponse().withStatus(200).withBody("init")
                .withHeader("Last-Modified", "Fri, 4 Oct 2019 18:58:30 GMT")));
        stubFor(path().withHeader("If-Modified-Since", equalTo("Fri, 4 Oct 2019 18:58:30 GMT"))
                .willReturn(aResponse().withStatus(304)));
        stubFor(path().withHeader("If-Modified-Since", equalTo("Fri, 4 Oct 2019 18:58:31 GMT"))
                .willReturn(aResponse().withStatus(200).withHeader("Last-Modified", "Fri, 4 Oct 2019 18:58:31 GMT")
                        .withBody("one")));

        var source = new HttpSource(client(), defaultURL(), CacheControlStrategy.IF_MODIFIED_SINCE);
        var loadedResource = source.load();
        assertThat(IOUtils.toString(loadedResource.getInputStream(), UTF_8)).isEqualTo("init");

        assertThat(source.loadIfModified(loadedResource.getVersion())).isNull();
        verify(exactly(1), getRequestedFor(urlPathEqualTo(PATH)).withHeader("If-Modified-Since",
                equalTo("Fri, 4 Oct 2019 18:58:30 GMT")));

        loadedResource = source.loadIfModified(new LastModified(1570215511000L));
        assertThat(loadedResource).isNotNull();
        assertThat(IOUtils.toString(loadedResource.getInputStream(), UTF_8)).isEqualTo("one");
    }

    @Test
    public void testETagScenario() throws Exception {
        stubFor(path().willReturn(aResponse().withStatus(200).withBody("init").withHeader("ETag", "1ab")));
        stubFor(path().withHeader("If-None-Match", equalTo("1ab")).willReturn(aResponse().withStatus(304)));
        stubFor(path().withHeader("If-None-Match", equalTo("1ac")).willReturn(aResponse().withStatus(200)
                .withHeader("ETag", "1ad").withBody("one")));

        var source = new HttpSource(client(), defaultURL(), CacheControlStrategy.ETAG);
        var loadedResource = source.load();
        assertThat(IOUtils.toString(loadedResource.getInputStream(), UTF_8)).isEqualTo("init");

        assertThat(source.loadIfModified(loadedResource.getVersion())).isNull();
        verify(exactly(1), getRequestedFor(urlPathEqualTo(PATH)).withHeader("If-None-Match", equalTo("1ab")));

        
    }

    private String defaultURL() {
        return url(PATH);
    }

    private MappingBuilder path() {
        return get(urlEqualTo(PATH));
    }

    private String url(String path) {
        return "http://127.0.0.1:" + wireMockServer.port() + path;
    }

    private CloseableHttpClient client() {
        return HttpClients.createMinimal();
    }

}

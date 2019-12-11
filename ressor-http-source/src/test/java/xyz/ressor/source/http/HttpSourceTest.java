package xyz.ressor.source.http;

import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.client.MappingBuilder;
import com.github.tomakehurst.wiremock.client.ResponseDefinitionBuilder;
import com.github.tomakehurst.wiremock.core.Options;
import org.apache.commons.io.IOUtils;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import xyz.ressor.source.LoadedResource;
import xyz.ressor.source.Source;
import xyz.ressor.source.SourceVersion;
import xyz.ressor.source.http.version.ETag;
import xyz.ressor.source.version.LastModified;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static java.nio.charset.StandardCharsets.UTF_8;
import static org.assertj.core.api.Assertions.assertThat;
import static xyz.ressor.source.http.CacheControlStrategy.*;

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
        stubFor(getPath()
                .willReturn(aResponse()
                        .withStatus(200).withBody("one")));

        HttpSource source = Http.source();
        HttpResourceId id = Http.url(defaultURL(), NONE);
        assertThat(IOUtils.toString(source.load(id).getInputStream(), UTF_8)).isEqualTo("one");
        assertThat(IOUtils.toString(source.loadIfModified(id, new LastModified(System.currentTimeMillis()))
                .getInputStream(), UTF_8)).isEqualTo("one");
    }

    @Test
    public void testIfModifiedSinceScenario() throws Exception {
        stubFor(getPath().willReturn(aResponse().withStatus(200).withBody("init")
                .withHeader("Last-Modified", "Fri, 4 Oct 2019 18:58:30 GMT")));
        stubFor(getPath().withHeader("If-Modified-Since", equalTo("Fri, 4 Oct 2019 18:58:30 GMT"))
                .willReturn(aResponse().withStatus(304)));
        stubFor(getPath().withHeader("If-Modified-Since", equalTo("Fri, 4 Oct 2019 18:58:31 GMT"))
                .willReturn(aResponse().withStatus(200).withHeader("Last-Modified", "Fri, 4 Oct 2019 18:58:32 GMT")
                        .withBody("one")));

        HttpSource source = Http.builder().pool(5, 10000).build();
        HttpResourceId id = Http.url(defaultURL(), IF_MODIFIED_SINCE);
        LoadedResource loadedResource = source.load(id);
        assertThat(IOUtils.toString(loadedResource.getInputStream(), UTF_8)).isEqualTo("init");

        assertThat(source.loadIfModified(id, loadedResource.getVersion())).isNull();
        verify(exactly(1), getRequestedFor(urlPathEqualTo(PATH)).withHeader("If-Modified-Since",
                equalTo("Fri, 4 Oct 2019 18:58:30 GMT")));

        loadedResource = source.loadIfModified(id, new LastModified(1570215511000L));
        assertThat(loadedResource).isNotNull();
        assertThat(IOUtils.toString(loadedResource.getInputStream(), UTF_8)).isEqualTo("one");
        assertThat((long) loadedResource.getVersion().val()).isEqualTo(1570215512000L);
    }

    @Test
    public void testETagScenario() throws Exception {
        stubFor(getPath().willReturn(aResponse().withStatus(200).withBody("init").withHeader("ETag", "1ab")));
        stubFor(getPath().withHeader("If-None-Match", equalTo("1ab")).willReturn(aResponse().withStatus(304)));
        stubFor(getPath().withHeader("If-None-Match", equalTo("1ac")).willReturn(aResponse().withStatus(200)
                .withHeader("ETag", "1ad").withBody("one")));

        HttpSource source = Http.builder().socketTimeoutMs(10000).connectTimeoutMs(10000).build();
        HttpResourceId id = Http.url(defaultURL(), ETAG);
        LoadedResource loadedResource = source.load(id);
        assertThat(IOUtils.toString(loadedResource.getInputStream(), UTF_8)).isEqualTo("init");

        assertThat(source.loadIfModified(id, loadedResource.getVersion())).isNull();
        verify(exactly(1), getRequestedFor(urlPathEqualTo(PATH)).withHeader("If-None-Match", equalTo("1ab")));

        loadedResource = source.loadIfModified(id, new ETag("1ac"));
        assertThat(loadedResource).isNotNull();
        assertThat(IOUtils.toString(loadedResource.getInputStream(), UTF_8)).isEqualTo("one");
        assertThat(loadedResource.getVersion().val().toString()).isEqualTo("1ad");
    }

    @Test
    public void testIfModifiedHeadScenario() throws Exception {
        ResponseDefinitionBuilder response = aResponse().withStatus(200).withHeader("Last-Modified", "Fri, 4 Oct 2019 18:58:30 GMT");
        stubFor(headPath().willReturn(response));
        stubFor(getPath().willReturn(response.withBody("init")));

        HttpSource source = new HttpSource(client());
        HttpResourceId id = Http.url(defaultURL(), CacheControlStrategy.LAST_MODIFIED_HEAD);
        LoadedResource loadedResource = source.load(id);
        assertThat(IOUtils.toString(loadedResource.getInputStream(), UTF_8)).isEqualTo("init");
        verify(exactly(0), headRequestedFor(urlPathEqualTo(PATH)));
        verify(exactly(1), getRequestedFor(urlPathEqualTo(PATH)));

        assertThat(source.loadIfModified(id, loadedResource.getVersion())).isNull();
        verify(exactly(1), headRequestedFor(urlPathEqualTo(PATH)));
        verify(exactly(1), getRequestedFor(urlPathEqualTo(PATH)));

        removeAllMappings();

        response = aResponse().withStatus(200).withHeader("Last-Modified", "Fri, 4 Oct 2019 18:58:31 GMT");
        stubFor(headPath().willReturn(response));
        stubFor(getPath().willReturn(response.withBody("one")));

        loadedResource = source.loadIfModified(id, loadedResource.getVersion());
        assertThat(loadedResource).isNotNull();
        assertThat(IOUtils.toString(loadedResource.getInputStream(), UTF_8)).isEqualTo("one");
    }

    @Test
    public void testETagHeadScenario() throws Exception {
        ResponseDefinitionBuilder response = aResponse().withStatus(200).withHeader("ETag", "1ab");
        stubFor(headPath().willReturn(response));
        stubFor(getPath().willReturn(response.withBody("init")));

        HttpSource source = Http.builder().receiveBufferSize(5).build();
        HttpResourceId id = Http.url(defaultURL(), ETAG_HEAD);
        LoadedResource loadedResource = source.load(id);
        assertThat(IOUtils.toString(loadedResource.getInputStream(), UTF_8)).isEqualTo("init");
        verify(exactly(0), headRequestedFor(urlPathEqualTo(PATH)));
        verify(exactly(1), getRequestedFor(urlPathEqualTo(PATH)));

        assertThat(source.loadIfModified(id, loadedResource.getVersion())).isNull();
        verify(exactly(1), headRequestedFor(urlPathEqualTo(PATH)));
        verify(exactly(1), getRequestedFor(urlPathEqualTo(PATH)));

        removeAllMappings();

        response = aResponse().withStatus(200).withHeader("ETag", "1ac");
        stubFor(headPath().withHeader("If-None-Match", equalTo("1ab")).willReturn(response));
        stubFor(getPath().willReturn(response.withBody("one")));

        loadedResource = source.loadIfModified(id, loadedResource.getVersion());
        assertThat(loadedResource).isNotNull();
        assertThat(IOUtils.toString(loadedResource.getInputStream(), UTF_8)).isEqualTo("one");
    }

    @Test
    public void testMixedImplementationsScenario() throws Exception {
        stubFor(getPath().willReturn(aResponse().withStatus(200).withBody("init")
                .withHeader("Last-Modified", "Fri, 4 Oct 2019 18:58:30 GMT")));

        HttpSource source = new HttpSource(client());
        HttpResourceId id = Http.url(defaultURL(), CacheControlStrategy.ETAG);
        LoadedResource loadedResource = source.load(id);
        assertThat(IOUtils.toString(loadedResource.getInputStream(), UTF_8)).isEqualTo("init");
        assertThat(loadedResource.getVersion()).isSameAs(SourceVersion.EMPTY);

        assertThat(source.loadIfModified(id, loadedResource.getVersion())).isNotNull();
    }

    private String defaultURL() {
        return url(PATH);
    }

    private MappingBuilder getPath() {
        return get(urlEqualTo(PATH));
    }

    private MappingBuilder headPath() {
        return head(urlEqualTo(PATH));
    }

    private String url(String path) {
        return "http://127.0.0.1:" + wireMockServer.port() + path;
    }

    private CloseableHttpClient client() {
        return HttpClients.createMinimal();
    }

}

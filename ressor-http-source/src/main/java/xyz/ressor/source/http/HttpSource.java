package xyz.ressor.source.http;

import org.apache.http.impl.client.CloseableHttpClient;
import xyz.ressor.source.LoadedResource;
import xyz.ressor.source.Source;
import xyz.ressor.source.SourceVersion;
import xyz.ressor.source.Subscription;

import java.time.ZoneId;
import java.util.function.Consumer;

public class HttpSource implements Source {
    private static final ZoneId GMT = ZoneId.of("GMT");
    private final CloseableHttpClient client;

    public HttpSource() {
        this.client = null;
    }

    @Override
    public LoadedResource loadIfModified(SourceVersion version) {
        return null;
    }

    @Override
    public boolean isListenable() {
        return false;
    }

    @Override
    public Subscription subscribe(Consumer<LoadedResource> listener) {
        return null;
    }
}

package xyz.ressor.source.http;

import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.config.SocketConfig;
import org.apache.http.impl.client.*;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;

import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

public class HttpSourceBuilder {
    private CloseableHttpClient client;
    private Integer socketTimeoutMs;
    private Integer connectTimeoutMs;
    private boolean alwaysFollowRedirects = true;
    private Integer receiveBufferSize;
    private Integer maxTotalConnections;
    private Integer inactiveTtlMs;
    private boolean keepAlive = true;
    private boolean contentCompression = false;
    private Consumer<HttpRequestBase> requestInterceptor = r -> {};

    public HttpSourceBuilder httpClient(CloseableHttpClient client) {
        this.client = client;
        return this;
    }

    public HttpSourceBuilder socketTimeoutMs(int socketTimeoutMs) {
        this.socketTimeoutMs = socketTimeoutMs;
        return this;
    }

    public HttpSourceBuilder connectTimeoutMs(int connectTimeoutMs) {
        this.connectTimeoutMs = connectTimeoutMs;
        return this;
    }

    public HttpSourceBuilder alwaysFollowRedirects(boolean alwaysFollowRedirects) {
        this.alwaysFollowRedirects = alwaysFollowRedirects;
        return this;
    }

    public HttpSourceBuilder keepAlive(boolean keepAlive) {
        this.keepAlive = keepAlive;
        return this;
    }

    public HttpSourceBuilder receiveBufferSize(int receiveBufferSize) {
        this.receiveBufferSize = receiveBufferSize;
        return this;
    }

    public HttpSourceBuilder pool(int maxTotalConnections, int inactiveTtlMs) {
        this.maxTotalConnections = maxTotalConnections;
        this.inactiveTtlMs = inactiveTtlMs;
        return this;
    }

    public HttpSourceBuilder contentCompression(boolean contentCompression) {
        this.contentCompression = contentCompression;
        return this;
    }

    public HttpSourceBuilder requestInterceptor(Consumer<HttpRequestBase> requestInterceptor) {
        this.requestInterceptor = requestInterceptor;
        return this;
    }

    public HttpSource build() {
        if (client == null) {
            HttpClientBuilder client = HttpClients.custom();
            SocketConfig.Builder socketConfig = SocketConfig.custom();
            if (receiveBufferSize != null) {
                socketConfig.setRcvBufSize(receiveBufferSize);
            }
            if (socketTimeoutMs != null) {
                socketConfig.setSoTimeout(socketTimeoutMs);
            }
            client.setDefaultSocketConfig(socketConfig.setSoKeepAlive(keepAlive).build());
            if (connectTimeoutMs != null) {
                client.setDefaultRequestConfig(RequestConfig.custom()
                        .setConnectTimeout(connectTimeoutMs)
                        .setConnectionRequestTimeout(connectTimeoutMs)
                        .setContentCompressionEnabled(contentCompression)
                        .build());
            } else {
                client.setDefaultRequestConfig(RequestConfig.custom().setContentCompressionEnabled(contentCompression).build());
            }
            if (alwaysFollowRedirects) {
                client.setRedirectStrategy(LaxRedirectStrategy.INSTANCE);
            } else {
                client.disableRedirectHandling();
            }
            if (keepAlive) {
                client.setKeepAliveStrategy(DefaultConnectionKeepAliveStrategy.INSTANCE)
                        .setConnectionReuseStrategy(DefaultClientConnectionReuseStrategy.INSTANCE);
            }
            if (maxTotalConnections != null) {
                PoolingHttpClientConnectionManager cm = new PoolingHttpClientConnectionManager(inactiveTtlMs, TimeUnit.MILLISECONDS);
                cm.setMaxTotal(maxTotalConnections);
                client.setConnectionManager(cm);
            }
            this.client = client.build();
        }
        return new HttpSource(client, requestInterceptor, keepAlive);
    }

}

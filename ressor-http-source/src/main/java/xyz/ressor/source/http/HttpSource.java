package xyz.ressor.source.http;

import org.apache.http.Header;
import org.apache.http.HttpHeaders;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpHead;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.message.BasicHeader;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import xyz.ressor.commons.utils.Exceptions;
import xyz.ressor.source.LoadedResource;
import xyz.ressor.source.Source;
import xyz.ressor.source.SourceVersion;
import xyz.ressor.source.Subscription;
import xyz.ressor.source.http.version.ETag;
import xyz.ressor.source.version.LastModified;

import java.io.InputStream;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.temporal.TemporalAccessor;
import java.util.Collections;
import java.util.List;
import java.util.function.Consumer;

import static java.time.ZoneOffset.UTC;
import static java.time.format.DateTimeFormatter.RFC_1123_DATE_TIME;
import static java.util.Collections.singletonList;
import static org.apache.http.HttpStatus.SC_OK;

public class HttpSource implements Source {
    private static final Logger log = LoggerFactory.getLogger(HttpSource.class);
    private static final ZoneId GMT = ZoneId.of("GMT");
    protected final CloseableHttpClient client;
    protected final String resourceURI;
    protected final CacheControlStrategy cacheControl;
    protected final Consumer<HttpRequestBase> requestInterceptor;
    protected final boolean keepAlive;

    public HttpSource(CloseableHttpClient client, String resourceURI, CacheControlStrategy cacheControl) {
        this(client, resourceURI, cacheControl, r -> {}, true);
    }

    public HttpSource(CloseableHttpClient client, String resourceURI, CacheControlStrategy cacheControl,
                      Consumer<HttpRequestBase> requestInterceptor, boolean keepAlive) {
        this.client = client;
        this.resourceURI = resourceURI;
        this.cacheControl = cacheControl;
        this.requestInterceptor = requestInterceptor;
        this.keepAlive = keepAlive;
    }

    @Override
    public LoadedResource loadIfModified(SourceVersion version) {
        if (version == SourceVersion.EMPTY || cacheControl == CacheControlStrategy.NONE) {
            return loadResource();
        } else if (cacheControl == CacheControlStrategy.ETAG) {
            return loadResource(singletonList(new BasicHeader(HttpHeaders.IF_NONE_MATCH, version.val().toString())));
        } else if (cacheControl == CacheControlStrategy.IF_MODIFIED_SINCE) {
            return loadResource(singletonList(new BasicHeader(HttpHeaders.IF_MODIFIED_SINCE, lastModifiedValue(version.val()))));
        } else if (cacheControl == CacheControlStrategy.LAST_MODIFIED_HEAD ||
                cacheControl == CacheControlStrategy.ETAG_HEAD) {
            if (isChanged(version)) {
                return loadResource();
            }
        }
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

    protected boolean isChanged(SourceVersion version) {
        try {
            var head = new HttpHead(resourceURI);
            intercept(head);
            var response = client.execute(head);

            if (cacheControl == CacheControlStrategy.ETAG_HEAD) {
                var eTag = getHeader(response, HttpHeaders.ETAG);
                return eTag == null || !eTag.equals(version.val());
            } else if (cacheControl == CacheControlStrategy.LAST_MODIFIED_HEAD) {
                var lastModified = getLastModified(response);
                return lastModified == null || lastModified.isAfter(Instant.ofEpochMilli(version.val()));
            } else {
                return true;
            }
        } catch (Throwable t) {
            throw Exceptions.wrap(t);
        }
    }

    protected LoadedResource loadResource() {
        return loadResource(Collections.emptyList());
    }

    protected LoadedResource loadResource(List<Header> headers) {
        try {
            var get = new HttpGet(resourceURI);
            headers.forEach(get::addHeader);
            intercept(get);
            var response = client.execute(get);
            if (response.getStatusLine() != null) {
                var statusCode = response.getStatusLine().getStatusCode();
                if (statusCode == SC_OK) {
                    SourceVersion version = SourceVersion.EMPTY;
                    if (cacheControl == CacheControlStrategy.ETAG || cacheControl == CacheControlStrategy.ETAG_HEAD) {
                        var eTag = getHeader(response, HttpHeaders.ETAG);
                        if (eTag != null) {
                            version = new ETag(eTag);
                        } else {
                            log.debug("Not recognized a ETag header from the response");
                        }
                    } else if (cacheControl == CacheControlStrategy.IF_MODIFIED_SINCE || cacheControl == CacheControlStrategy.LAST_MODIFIED_HEAD) {
                        var lastModified = getLastModified(response);
                        if (lastModified != null) {
                            version = new LastModified(lastModified.toEpochMilli());
                        } else {
                            log.debug("Not recognized a Last-Modified header from the response");
                        }
                    }
                    return new LoadedResource(response.getEntity() != null ? response.getEntity().getContent() : InputStream.nullInputStream(),
                            version, resourceURI);
                } else {
                    log.debug("Received {} status code for GET {}", statusCode, resourceURI);
                }
            } else {
                log.debug("No status line received for GET {}", resourceURI);
            }
            return null;
        } catch (Throwable t) {
            throw Exceptions.wrap(t);
        }
    }

    protected String getHeader(CloseableHttpResponse httpResult, String name) {
        var header = httpResult.getFirstHeader(name);
        if (header != null && header.getValue().length() > 0) {
            return header.getValue();
        } else {
            return null;
        }
    }

    protected Instant getLastModified(CloseableHttpResponse httpResult) {
        TemporalAccessor ta = null;
        var header = httpResult.getFirstHeader(HttpHeaders.LAST_MODIFIED);
        if (header != null && header.getValue().length() > 0) {
            try {
                ta = RFC_1123_DATE_TIME.parse(header.getValue());
            } catch (Throwable t) {
                log.debug(t.getMessage(), t);
            }
        }
        return ta == null ? null : Instant.from(ta);
    }

    protected void intercept(HttpRequestBase req) {
        requestInterceptor.accept(req);
        if (keepAlive) {
            req.addHeader(HttpHeaders.CONNECTION, "keep-alive");
        }
    }

    protected String lastModifiedValue(long timestamp) {
        return RFC_1123_DATE_TIME.format(toDateTime(timestamp));
    }

    private ZonedDateTime toDateTime(long timestamp) {
        return ZonedDateTime.of(LocalDateTime.ofInstant(Instant.ofEpochMilli(timestamp), UTC), GMT);
    }
}

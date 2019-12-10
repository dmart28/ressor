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
import xyz.ressor.source.*;
import xyz.ressor.source.http.version.ETag;
import xyz.ressor.source.version.LastModified;

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
import static xyz.ressor.commons.utils.RessorUtils.emptyInputStream;

public class HttpSource extends AbstractSource<HttpResourceId> implements NonListenableSource<HttpResourceId> {
    private static final Logger log = LoggerFactory.getLogger(HttpSource.class);
    private static final ZoneId GMT = ZoneId.of("GMT");
    protected final CloseableHttpClient client;
    protected final Consumer<HttpRequestBase> requestInterceptor;
    protected final boolean keepAlive;

    public HttpSource(CloseableHttpClient client) {
        this(client, r -> {}, true);
    }

    public HttpSource(CloseableHttpClient client,
                      @NotNull Consumer<HttpRequestBase> requestInterceptor, boolean keepAlive) {
        this.client = client;
        this.requestInterceptor = requestInterceptor;
        this.keepAlive = keepAlive;
    }

    @Override
    public LoadedResource loadIfModified(HttpResourceId resourceId, SourceVersion version) {
        CacheControlStrategy cacheControl = resourceId.getCacheControl();
        if (version == SourceVersion.EMPTY || cacheControl == CacheControlStrategy.NONE) {
            return loadResource(resourceId);
        } else if (cacheControl == CacheControlStrategy.ETAG) {
            return loadResource(resourceId, singletonList(new BasicHeader(HttpHeaders.IF_NONE_MATCH, version.val().toString())));
        } else if (cacheControl == CacheControlStrategy.IF_MODIFIED_SINCE) {
            return loadResource(resourceId, singletonList(new BasicHeader(HttpHeaders.IF_MODIFIED_SINCE, lastModifiedValue(version.val()))));
        } else if (cacheControl == CacheControlStrategy.LAST_MODIFIED_HEAD ||
                cacheControl == CacheControlStrategy.ETAG_HEAD) {
            if (isChanged(resourceId, version)) {
                return loadResource(resourceId);
            }
        }
        return null;
    }

    @Override
    public String describe() {
        return "Http";
    }

    protected boolean isChanged(HttpResourceId resourceId, SourceVersion version) {
        try {
            HttpHead head = new HttpHead(resourceId.getResourceURI());
            intercept(head);
            CloseableHttpResponse response = client.execute(head);

            if (resourceId.getCacheControl() == CacheControlStrategy.ETAG_HEAD) {
                String eTag = getHeader(response, HttpHeaders.ETAG);
                return eTag == null || !eTag.equals(version.val());
            } else if (resourceId.getCacheControl() == CacheControlStrategy.LAST_MODIFIED_HEAD) {
                Instant lastModified = getLastModified(response);
                return lastModified == null || lastModified.isAfter(Instant.ofEpochMilli(version.val()));
            } else {
                return true;
            }
        } catch (Throwable t) {
            throw Exceptions.wrap(t);
        }
    }

    protected LoadedResource loadResource(HttpResourceId resourceId) {
        return loadResource(resourceId, Collections.emptyList());
    }

    protected LoadedResource loadResource(HttpResourceId resourceId, List<Header> headers) {
        try {
            String resourceURI = resourceId.getResourceURI();
            CacheControlStrategy cacheControl = resourceId.getCacheControl();
            HttpGet get = new HttpGet(resourceURI);
            headers.forEach(get::addHeader);
            intercept(get);
            CloseableHttpResponse response = client.execute(get);
            if (response.getStatusLine() != null) {
                int statusCode = response.getStatusLine().getStatusCode();
                if (statusCode == SC_OK) {
                    SourceVersion version = SourceVersion.EMPTY;
                    if (cacheControl == CacheControlStrategy.ETAG || cacheControl == CacheControlStrategy.ETAG_HEAD) {
                        String eTag = getHeader(response, HttpHeaders.ETAG);
                        if (eTag != null) {
                            version = new ETag(eTag);
                        } else {
                            log.debug("Not recognized a ETag header from the response");
                        }
                    } else if (cacheControl == CacheControlStrategy.IF_MODIFIED_SINCE || cacheControl == CacheControlStrategy.LAST_MODIFIED_HEAD) {
                        Instant lastModified = getLastModified(response);
                        if (lastModified != null) {
                            version = new LastModified(lastModified.toEpochMilli());
                        } else {
                            log.debug("Not recognized a Last-Modified header from the response");
                        }
                    }
                    return new LoadedResource(response.getEntity() != null ? response.getEntity().getContent() : emptyInputStream(),
                            version, resourceId);
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
        Header header = httpResult.getFirstHeader(name);
        if (header != null && header.getValue().length() > 0) {
            return header.getValue();
        } else {
            return null;
        }
    }

    protected Instant getLastModified(CloseableHttpResponse httpResult) {
        TemporalAccessor ta = null;
        Header header = httpResult.getFirstHeader(HttpHeaders.LAST_MODIFIED);
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

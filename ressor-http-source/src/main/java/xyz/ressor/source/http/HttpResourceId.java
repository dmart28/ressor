package xyz.ressor.source.http;

import xyz.ressor.source.ResourceId;

public class HttpResourceId implements ResourceId {
    private final String resourceURI;
    private final CacheControlStrategy cacheControl;

    public HttpResourceId(String resourceURI, CacheControlStrategy cacheControl) {
        this.resourceURI = resourceURI;
        this.cacheControl = cacheControl;
    }

    public String getResourceURI() {
        return resourceURI;
    }

    public CacheControlStrategy getCacheControl() {
        return cacheControl;
    }

    @Override
    public String describe() {
        return resourceURI;
    }

    @Override
    public Class<?> sourceType() {
        return HttpSource.class;
    }
}

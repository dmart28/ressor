package xyz.ressor.source.http;

public class Http {

    public static HttpSourceBuilder builder() {
        return new HttpSourceBuilder();
    }

    public static HttpSource source() {
        return builder().build();
    }

    public static HttpResourceId id(String resourceURI) {
        return new HttpResourceId(resourceURI, CacheControlStrategy.ETAG);
    }

    public static HttpResourceId id(String resourceURI, CacheControlStrategy cacheControl) {
        return new HttpResourceId(resourceURI, cacheControl);
    }

}

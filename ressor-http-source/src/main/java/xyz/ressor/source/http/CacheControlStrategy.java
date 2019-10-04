package xyz.ressor.source.http;

public enum CacheControlStrategy {
    ETAG,
    IF_MODIFIED_SINCE,
    LAST_MODIFIED_HEAD,
    ETAG_HEAD,
    NONE
}

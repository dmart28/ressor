package xyz.ressor.source.http;

/**
 * The strategy which defines the way {@link HttpSource} will know about whether the underlying resource was changed.
 */
public enum CacheControlStrategy {
    /**
     * Ressor will use ETag header as a version snapshot of the resource. It will send the If-None-Match header
     * with the last version value in order to receive only the modified body.
     */
    ETAG,
    /**
     * Ressor will use Last-Modified header as a version snapshot of the resource. It will send the If-Modified-Since
     * header with the last version value in order to receive only the modified body.
     */
    IF_MODIFIED_SINCE,
    /**
     * Same as ETAG, but for servers which doesn't support If-None-Match header. Must support HEAD requests.
     */
    LAST_MODIFIED_HEAD,
    /**
     * Same as IF-MODIFIED-SINCE, but for servers which doesn't support If-Modified-Since header. Must support HEAD requests.
     */
    ETAG_HEAD,
    /**
     * Always reload the resource on every poll.
     */
    NONE
}

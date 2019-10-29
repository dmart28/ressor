package xyz.ressor.config;

import java.util.concurrent.ExecutorService;

/**
 * Global configurations for the Ressor
 */
public class RessorConfig {
    private Integer pollingThreads;
    private ExecutorService threadPool;
    private Integer reloadRetryMaxMillis;
    private Boolean cacheClasses;

    public RessorConfig() {
    }

    public RessorConfig(RessorConfig other) {
        this.pollingThreads = other.pollingThreads == null? Runtime.getRuntime().availableProcessors() : other.pollingThreads;
        this.threadPool = other.threadPool;
        this.reloadRetryMaxMillis = other.reloadRetryMaxMillis == null ? 64_000 : other.reloadRetryMaxMillis;
        this.cacheClasses = other.cacheClasses == null ? true : other.cacheClasses;
    }

    /**
     * The amount of threads used for polling.
     *
     * Default is available processors count
     */
    public Integer pollingThreads() {
        return pollingThreads;
    }

    /**
     * The thread pool which is used by Ressor services for loading the resources data.
     *
     * Default is thread pool of available processors size
     */
    public ExecutorService threadPool() {
        return threadPool;
    }

    /**
     * The maximum time for which the listener will wait until it give up to retry on reload by signal.
     *
     * Make sense when the signal from listener arrived, but service reload failed for some reason
     *
     * Default is 64 seconds
     */
    public Integer reloadRetryMaxMillis() {
        return reloadRetryMaxMillis;
    }

    /**
     * Whether to cache generated proxy classes if possible.
     *
     * Default is true
     */
    public Boolean isCacheClasses() {
        return cacheClasses;
    }

    public RessorConfig pollingThreads(int pollingThreads) {
        this.pollingThreads = pollingThreads;
        return this;
    }

    public RessorConfig threadPool(ExecutorService threadPool) {
        this.threadPool = threadPool;
        return this;
    }

    public RessorConfig reloadRetryMaxMillis(int reloadRetryMaxMillis) {
        this.reloadRetryMaxMillis = reloadRetryMaxMillis;
        return this;
    }

    public RessorConfig cacheClasses(boolean cacheClasses) {
        this.cacheClasses = cacheClasses;
        return this;
    }
}

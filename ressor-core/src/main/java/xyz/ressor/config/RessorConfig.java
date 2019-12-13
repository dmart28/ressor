package xyz.ressor.config;

import xyz.ressor.service.error.ErrorHandler;

import java.util.concurrent.ExecutorService;

/**
 * Global configurations for the Ressor
 */
public class RessorConfig {
    private Integer pollingThreads;
    private ExecutorService threadPool;
    private Boolean cacheClasses;
    private ErrorHandler errorHandler;

    public RessorConfig() {
    }

    public RessorConfig(RessorConfig other) {
        this.pollingThreads = other.pollingThreads == null? Runtime.getRuntime().availableProcessors() : other.pollingThreads;
        this.threadPool = other.threadPool;
        this.cacheClasses = other.cacheClasses == null ? true : other.cacheClasses;
        this.errorHandler = other.errorHandler;
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
     * Whether to cache generated proxy classes if possible.
     *
     * Default is true
     */
    public Boolean isCacheClasses() {
        return cacheClasses;
    }

    /**
     * Global error handler, which will be used in the absence of per-service error handler
     */
    public ErrorHandler errorHandler() {
        return errorHandler;
    }

    public RessorConfig pollingThreads(int pollingThreads) {
        this.pollingThreads = pollingThreads;
        return this;
    }

    public RessorConfig threadPool(ExecutorService threadPool) {
        this.threadPool = threadPool;
        return this;
    }

    public RessorConfig cacheClasses(boolean cacheClasses) {
        this.cacheClasses = cacheClasses;
        return this;
    }

    public RessorConfig errorHandler(ErrorHandler errorHandler) {
        this.errorHandler = errorHandler;
        return this;
    }
}

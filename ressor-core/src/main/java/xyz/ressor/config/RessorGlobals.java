package xyz.ressor.config;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Global configurations for the Ressor
 */
public class RessorGlobals {
    private static final Object lock = new Object();
    private static volatile RessorGlobals instance;
    private volatile int pollingThreads = Runtime.getRuntime().availableProcessors();
    private volatile ExecutorService threadPool = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());

    private RessorGlobals() {
    }

    /**
     * The amount of threads used for polling
     */
    public int pollingThreads() {
        return pollingThreads;
    }

    public RessorGlobals pollingThreads(int pollingThreads) {
        this.pollingThreads = pollingThreads;
        return this;
    }

    /**
     * The thread pool which is used by Ressor services for loading the resources data
     */
    public ExecutorService threadPool() {
        return threadPool;
    }

    public RessorGlobals threadPool(ExecutorService threadPool) {
        this.threadPool = threadPool;
        return this;
    }

    public static RessorGlobals getInstance() {
        if (instance == null) {
            synchronized (lock) {
                if (instance == null) {
                    instance = new RessorGlobals();
                }
            }
        }
        return instance;
    }
}

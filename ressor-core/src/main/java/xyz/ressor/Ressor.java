package xyz.ressor;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import xyz.ressor.commons.watch.fs.FileSystemWatchService;
import xyz.ressor.config.RessorConfig;
import xyz.ressor.loader.ListeningServiceLoader;
import xyz.ressor.loader.QuartzManager;
import xyz.ressor.loader.ServiceLoaderBase;
import xyz.ressor.service.RessorService;
import xyz.ressor.service.proxy.RessorServiceImpl;
import xyz.ressor.source.Source;

import java.util.concurrent.Executors;
import java.util.function.Consumer;
import java.util.function.Function;

import static xyz.ressor.service.proxy.StateVariables.LOADER;
import static xyz.ressor.service.proxy.StateVariables.SOURCE;

/**
 * Public API of the Ressor framework.
 *
 * Ressor is a framework which ease the development of resource-based Java services. It translates your static or dynamic resources (files, http endpoints, git repositories, etc) into a complete Java service instance, implicitly reloading it when the source data is changed.
 * It supports various formats as well as different kinds of data sources.
 */
public class Ressor {
    private static final Logger log = LoggerFactory.getLogger(Ressor.class);
    private final QuartzManager quartzManager;
    private final FileSystemWatchService fsWatchService;
    private final RessorConfig config;

    public static Ressor create() {
        return create(new RessorConfig());
    }

    public static Ressor create(RessorConfig config) {
        return new Ressor(config);
    }

    private Ressor(RessorConfig c) {
        this.config = new RessorConfig(c);
        if (config.threadPool() == null) {
            config.threadPool(Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors()));
        }
        this.quartzManager = new QuartzManager(config.pollingThreads());
        this.fsWatchService = new FileSystemWatchService().init();
    }

    /**
     * Creates a new service builder for the given {@param type}, which can be either interface or class.
     *
     * @param type the public type of your service for which proxy class would be generated and
     *            which instance would be created as a result
     * @param <T> the public service type
     * @return proxy class instance
     */
    public <T> RessorBuilder<T> service(Class<T> type) {
        return new RessorBuilder<>(type, config, fsWatchService);
    }

    /**
     * Starts listening the {@link Source} of the underlying {@param service} for changes. When the change event is fired,
     * the {@param service} will be implicitly reloaded with the new data.
     *
     * Please note that for now listening is supported only by {@link xyz.ressor.source.fs.FileSystemSource},
     * otherwise the appropriate exception would be thrown
     *
     * @param service Ressor proxy service instance
     * @param <T> service public type
     */
    public <T> void listen(T service) {
        checkRessorService(service, ressorService -> {
            checkAndStopLoaderIfRequired(ressorService);
            var loader = new ListeningServiceLoader(ressorService, (Source) ressorService.state(SOURCE), config.threadPool(),
                    config.reloadRetryMaxMillis());
            ressorService.state(LOADER, loader);
        });
    }

    /**
     * Starts polling the {@link Source} of the underlying {@param service} for the incoming changes, and in case of any, reloading the {@param service}.
     *
     * Polling is supported by every {@link Source}, and have different approaches based on the implementation. Please
     * read the documentation of child classes.
     *
     * @param service Ressor proxy service instance
     * @param <T> service public type
     * @return Polling builder
     */
    public <T> PollingBuilder poll(T service) {
        return checkRessorService(service, ressorService -> {
            checkAndStopLoaderIfRequired(ressorService);
            return new PollingBuilder(ressorService, config.threadPool(), quartzManager);
        });
    }

    /**
     * Stop any periodic activity on the service (polling or listening)
     *
     * @param service Ressor proxy service instance
     * @param <T> service public type
     */
    public <T> void stop(T service) {
        checkRessorService(service, Ressor::checkAndStopLoaderIfRequired);
    }

    public void shutdown() {
        try {
            quartzManager.scheduler().shutdown();
            fsWatchService.destroy();
            config.threadPool().shutdownNow();
        } catch (Throwable t) {
            log.error("Failed to completely shutdown Ressor: {}", t.getMessage(), t);
        }
    }

    private static <T> void checkRessorService(T service, Consumer<RessorServiceImpl> action) {
        checkRessorService(service, (Function<RessorServiceImpl, Void>) rs -> {
           action.accept(rs);
           return null;
        });
    }

    private static void checkAndStopLoaderIfRequired(RessorServiceImpl ressorService) {
        var loader = ressorService.state(LOADER);
        if (loader != null) {
            ((ServiceLoaderBase) loader).stop();
            ressorService.state(LOADER, null);
        }
    }

    private static <T, R> R checkRessorService(T service, Function<RessorServiceImpl, R> action) {
        if (service instanceof RessorService) {
            return action.apply((RessorServiceImpl) ((RessorService) service).unwrap());
        } else {
            throw new IllegalArgumentException("Provided service is not generated with Ressor");
        }
    }

}

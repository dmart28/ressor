package xyz.ressor;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import xyz.ressor.commons.watch.fs.FileSystemWatchService;
import xyz.ressor.config.RessorConfig;
import xyz.ressor.loader.ListeningServiceLoader;
import xyz.ressor.loader.QuartzManager;
import xyz.ressor.loader.ServiceLoaderBase;
import xyz.ressor.service.RessorService;
import xyz.ressor.service.ServiceManager;
import xyz.ressor.service.proxy.RessorServiceImpl;
import xyz.ressor.source.Source;
import xyz.ressor.source.fs.FileSystemSource;

import java.util.concurrent.Executors;
import java.util.function.Consumer;
import java.util.function.Function;

import static xyz.ressor.service.proxy.StateHelper.getSource;
import static xyz.ressor.service.proxy.StateVariables.LOADER;
import static xyz.ressor.service.proxy.StateVariables.SOURCE;

/**
 * Public API of the Ressor framework.
 * <p/>
 * Ressor instances can be created using default configuration with {@link #create()} or a custom one with {@link #create(RessorConfig)}.
 * <p/>
 * It's recommended (though not restricted) to have a single Ressor instance per running VM and re-use it everywhere.
 */
public class Ressor {
    private static final Logger log = LoggerFactory.getLogger(Ressor.class);
    private final QuartzManager quartzManager;
    private final FileSystemSource fileSystemSource;
    private final FileSystemWatchService fsWatchService;
    private final RessorConfig config;
    private final ActionsManager actionsManager;
    private final ServiceManager serviceManager;

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
        this.fileSystemSource = new FileSystemSource(fsWatchService);
        this.serviceManager = new ServiceManager(config);
        this.actionsManager = new ActionsManager(serviceManager, config.threadPool());
    }

    /**
     * Creates a new service builder for the given type, which can be either interface or class.
     *
     * @param type the public type of your service for which proxy instance would be generated and returned as a result
     * @param <T> the public service type
     * @return service builder instance
     */
    public <T> RessorBuilder<T, Object> service(Class<T> type) {
        return new RessorBuilder<>(type, config, fileSystemSource, serviceManager);
    }

    /**
     * Starts listening the {@link Source} of the underlying service for the changes. When the change event is received,
     * the service will implicitly reload itself with the new resource.
     *
     * Please note that for now listening is supported only by {@link xyz.ressor.source.fs.FileSystemSource}.
     * Unless, an appropriate exception would be thrown.
     *
     * @param service Ressor service proxy instance
     * @param <T> service public type
     */
    public <T> void listen(T service) {
        checkRessorService(service, ressorService -> {
            checkAndStopLoaderIfRequired(ressorService);
            ListeningServiceLoader loader = new ListeningServiceLoader(serviceManager, ressorService, (Source) ressorService.state(SOURCE));
            ressorService.state(LOADER, loader);
        });
    }

    /**
     * Starts polling the {@link Source} of the underlying service for incoming changes. When resource is detected to be
     * changed, the service will implicitly reload itself with the new resource.
     *
     * Polling is supported by every {@link Source}, and have different approaches based on the implementation. Please
     * read documentation of each source implementation.
     *
     * @param service Ressor service proxy instance
     * @param <T> service public type
     * @return poller builder instance
     */
    public <T> PollingBuilder poll(T service) {
        return checkRessorService(service, ressorService -> {
            checkAndStopLoaderIfRequired(ressorService);
            return new PollingBuilder(ressorService, serviceManager, quartzManager);
        });
    }

    /**
     * Forces the service to reload from the underlying data source. This methods blocks until the reload completion.
     *
     * @param service Ressor service to be reloaded
     * @param <T> service public type
     */
    public <T> void reload(T service) {
        checkRessorService(service, ressorService -> {
            serviceManager.reload(ressorService, getSource(ressorService));
        });
    }

    /**
     * Schedules the service reload from the underlying data source. Unlike {@link #reload(Object)}, this
     * method return immediately without waiting for the reload completion.
     *
     * @param service Ressor service to be reloaded
     * @param <T> service public type
     */
    public <T> void scheduleReload(T service) {
        checkRessorService(service, ressorService -> {
            serviceManager.reloadAsync(ressorService, getSource(ressorService));
        });
    }

    public ActionsManager actions() {
        return actionsManager;
    }

    /**
     * Stops any periodic activity on the service (polling or listening).
     *
     * @param service Ressor service proxy instance
     * @param <T> service public type
     */
    public <T> void stop(T service) {
        checkRessorService(service, Ressor::checkAndStopLoaderIfRequired);
    }

    /**
     * Shutdowns the current Ressor context. This includes stopping all polling, listening activities,
     * as well as service reloads and associated actions. The internal thread pool is shutdown as well.
     * <p />
     * This operation doesn't affect the created services itself, they will be usable until they are
     * normally garbage collected by VM. Ressor doesn't keep any strong references to them.
     */
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
        ServiceLoaderBase loader = (ServiceLoaderBase) ressorService.state(LOADER);
        if (loader != null) {
            loader.stop();
            ressorService.state(LOADER, null);
        }
    }

    static <T, R> R checkRessorService(T service, Function<RessorServiceImpl, R> action) {
        if (service instanceof RessorService) {
            return action.apply((RessorServiceImpl) ((RessorService) service).unwrap());
        } else {
            throw new IllegalArgumentException("Provided service is not generated with Ressor");
        }
    }

}

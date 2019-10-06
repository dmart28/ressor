package xyz.ressor;

import xyz.ressor.config.RessorGlobals;
import xyz.ressor.loader.ListeningServiceLoader;
import xyz.ressor.loader.ServiceLoaderBase;
import xyz.ressor.service.RessorService;
import xyz.ressor.service.proxy.RessorServiceImpl;
import xyz.ressor.source.Source;

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

    /**
     * Ressor global configuration, shared by all internal components.
     * If global properties are being changed after calling methods like {@link #poll(Object)},
     * {@link #listen(Object)}, etc. it's not guaranteed that they will be applied after all.
     */
    public static RessorGlobals globals() {
        return RessorGlobals.getInstance();
    }

    /**
     * Creates a new service builder for the given {@param type}, which can be either interface or class.
     *
     * @param type the public type of your service for which proxy class would be generated and
     *            which instance would be created as a result
     * @param <T> the public service type
     * @return proxy class instance
     */
    public static <T> RessorBuilder<T> service(Class<T> type) {
        return new RessorBuilder<>(type);
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
    public static <T> void listen(T service) {
        checkRessorService(service, ressorService -> {
            checkAndStopLoaderIfRequired(ressorService);
            var loader = new ListeningServiceLoader(ressorService, (Source) ressorService.state(SOURCE));
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
    public static <T> PollingBuilder poll(T service) {
        return checkRessorService(service, ressorService -> {
            checkAndStopLoaderIfRequired(ressorService);
            return new PollingBuilder(ressorService);
        });
    }

    /**
     * Stop any periodic activity on the service (polling or listening)
     *
     * @param service Ressor proxy service instance
     * @param <T> service public type
     */
    public static <T> void stop(T service) {
        checkRessorService(service, Ressor::checkAndStopLoaderIfRequired);
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

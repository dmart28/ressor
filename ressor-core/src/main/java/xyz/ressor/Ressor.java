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

public class Ressor {

    public static RessorGlobals globals() {
        return RessorGlobals.getInstance();
    }

    public static <T> RessorBuilder<T> service(Class<T> type) {
        return new RessorBuilder<>(type);
    }

    public static <T> void listen(T service) {
        checkRessorService(service, ressorService -> {
            checkAndStopLoaderIfRequired(ressorService);
            var loader = new ListeningServiceLoader(ressorService, (Source) ressorService.state(SOURCE));
            ressorService.state(LOADER, loader);
        });
    }

    public static <T> PollingBuilder poll(T service) {
        return checkRessorService(service, ressorService -> {
            checkAndStopLoaderIfRequired(ressorService);
            return new PollingBuilder(ressorService);
        });
    }

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

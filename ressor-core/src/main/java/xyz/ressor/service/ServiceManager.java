package xyz.ressor.service;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import xyz.ressor.config.RessorConfig;
import xyz.ressor.source.LoadedResource;
import xyz.ressor.source.Source;

import java.util.concurrent.CompletableFuture;

public class ServiceManager {
    private final RessorConfig config;

    public ServiceManager(RessorConfig config) {
        this.config = config;
    }

    public boolean tryReload(RessorService service, Source source) {
        return doReload(service, source, false);
    }

    public CompletableFuture<Boolean> tryReloadAsync(RessorService service, Source source) {
        return doReloadAsync(service, source, false);
    }

    public boolean reload(RessorService service, Source source) {
        return doReload(service, source, true);
    }

    public CompletableFuture<Boolean> reloadAsync(RessorService service, Source source) {
        return doReloadAsync(service, source, true);
    }

    @NotNull
    private CompletableFuture<Boolean> doReloadAsync(RessorService service, Source source, boolean isForce) {
        final CompletableFuture<Boolean> result = new CompletableFuture<>();
        config.threadPool().submit(() -> {
            try {
                result.complete(doReload(service, source, isForce));
            } catch (Throwable t) {
                result.completeExceptionally(t);
            }
        });
        return result;
    }

    private boolean doReload(RessorService service, Source source, boolean isForce) {
        LoadedResource resource = loadResourceFromSource(service, source, isForce);
        try {
            return service.reload(resource, isForce);
        } catch (Throwable t) {
            if (service.errorHandler() != null) {
                service.errorHandler().onTranslateFailed(t, resource, service.safeInstance());
                return false;
            } else {
                throw t;
            }
        }
    }

    @Nullable
    private LoadedResource loadResourceFromSource(RessorService service, Source source, boolean isForce) {
        LoadedResource resource = null;
        try {
            if (isForce) {
                resource = source.load(service.getResourceId());
            } else if (!service.isReloading()) {
                resource = source.loadIfModified(service.getResourceId(), service.latestVersion());
            }
        } catch (Throwable t) {
            if (service.errorHandler() != null) {
                service.errorHandler().onSourceFailed(t, service.safeInstance());
            } else {
                throw t;
            }
        }
        return resource;
    }

}

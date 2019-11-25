package xyz.ressor.loader;

import org.jetbrains.annotations.Nullable;
import xyz.ressor.service.RessorService;
import xyz.ressor.source.LoadedResource;
import xyz.ressor.source.Source;

import java.util.function.BiFunction;

public class LoaderHelper {

    @Nullable
    public static LoadedResource loadFromSource(RessorService service, Source source) {
        return loadFromSource(service, source, (src, s) -> src.load());
    }

    @Nullable
    public static LoadedResource loadFromSource(RessorService service, Source source, BiFunction<Source, RessorService, LoadedResource> reloader) {
        LoadedResource resource = null;
        try {
            resource = reloader.apply(source, service);
        } catch (Throwable t) {
            if (service.errorHandler() != null) {
                service.errorHandler().onSourceFailed(t, service);
            } else {
                throw t;
            }
        }
        return resource;
    }

    public static boolean reload(RessorService service, Source source) {
        return reload(service, loadFromSource(service, source));
    }

    public static boolean reload(RessorService service, LoadedResource resource) {
        try {
            return service.reload(resource);
        } catch (Throwable t) {
            if (service.errorHandler() != null) {
                service.errorHandler().onTranslateFailed(t, resource, service);
                return false;
            } else {
                throw t;
            }
        }
    }

}

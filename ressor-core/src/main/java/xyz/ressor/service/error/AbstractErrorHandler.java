package xyz.ressor.service.error;

import org.jetbrains.annotations.Nullable;
import xyz.ressor.source.LoadedResource;

public abstract class AbstractErrorHandler implements ErrorHandler {

    @Override
    public void onSourceFailed(Throwable exception, @Nullable Object service) {
    }

    @Override
    public void onTranslateFailed(Throwable exception, LoadedResource resource, @Nullable Object service) {
    }
}

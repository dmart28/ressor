package xyz.ressor.service.error;

import org.jetbrains.annotations.Nullable;
import xyz.ressor.source.LoadedResource;

import java.util.function.Consumer;

public class SimpleErrorHandler extends AbstractErrorHandler {
    private final Consumer<Throwable> handler;

    public SimpleErrorHandler(Consumer<Throwable> handler) {
        this.handler = handler;
    }

    @Override
    public void onSourceFailed(Throwable exception, @Nullable Object service) {
        handler.accept(exception);
    }

    @Override
    public void onTranslateFailed(Throwable exception, LoadedResource resource, @Nullable Object service) {
        handler.accept(exception);
    }
}

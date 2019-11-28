package xyz.ressor.service.error;

import xyz.ressor.source.LoadedResource;

/**
 * The exception handler for such Ressor activities like loading data from {@link xyz.ressor.source.Source},
 * translation of the loaded data, etc.
 */
public interface ErrorHandler {

    /**
     * Called when the {@link xyz.ressor.source.Source} failed to load given resource and threw an exception
     *
     * @param exception an exception which was thrown by the source
     * @param service service proxied instance
     */
    void onSourceFailed(Throwable exception, Object service);

    /**
     * Called when the {@link xyz.ressor.source.Source} started returning data, but eventually some error
     * in the given {@link xyz.ressor.translator.Translator} occurred.
     *
     * @param exception an exception which was thrown
     * @param resource the resource
     * @param service service proxied instance
     */
    void onTranslateFailed(Throwable exception, LoadedResource resource, Object service);

}

package xyz.ressor.service;

import xyz.ressor.source.LoadedResource;
import xyz.ressor.source.SourceVersion;

public interface RessorService<T> {

    /**
     * Returns the original instance, which implements {@link RessorService} instance, since actual services doesn't
     * directly extend them.
     */
    RessorService<T> unwrap();

    /**
     * Returns the underlying type of the user service.
     */
    Class<? extends T> underlyingType();

    /**
     * Unwrapped latest instance of the user service.
     */
    T instance();

    /**
     * Returns the latest resource version which this service is aware of and is switching to.
     *
     * It's guaranteed that instance() and latestVersion() will be *eventually in sync* with each other at some
     * point of time.
     */
    SourceVersion latestVersion();

    /**
     * Attempts to reload the service with the given {@param resource}.
     *
     * @param resource the new version of the resource
     * @return true if the resource was successfully applied, unless false
     */
    boolean reload(LoadedResource resource);

    boolean isReloading();

}

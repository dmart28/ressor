package xyz.ressor.service;

import xyz.ressor.source.LoadedResource;
import xyz.ressor.source.SourceVersion;

public interface RessorService<T> {

    RessorService<T> unwrap();

    Class<? extends T> underlyingType();

    T instance();

    /**
     * Returns the latest resource version which this service is aware of and is switching to.
     *
     * It's guaranteed that instance() and latestVersion() will be *eventually in sync* with each other at some
     * point of time.
     */
    SourceVersion latestVersion();

    default void reload(LoadedResource resource) {
        reload(resource, false);
    }

    void reload(LoadedResource resource, boolean force);

}

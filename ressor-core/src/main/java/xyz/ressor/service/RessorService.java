package xyz.ressor.service;

import xyz.ressor.source.LoadedResource;
import xyz.ressor.source.SourceVersion;

public interface RessorService<T> {

    Class<? extends T> underlyingType();

    T instance();

    SourceVersion currentVersion();

    void reload(LoadedResource resource);

}

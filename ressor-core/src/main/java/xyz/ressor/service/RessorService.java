package xyz.ressor.service;

import xyz.ressor.source.LoadedResource;
import xyz.ressor.source.SourceVersion;

public interface RessorService<T> {

    RessorService<T> unwrap();

    Class<? extends T> underlyingType();

    T instance();

    SourceVersion currentVersion();

    void reload(LoadedResource resource);

}

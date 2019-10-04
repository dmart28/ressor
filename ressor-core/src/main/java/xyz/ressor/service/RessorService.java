package xyz.ressor.service;

import xyz.ressor.source.LoadedResource;

public interface RessorService<T> {

    Class<? extends T> underlyingType();

    T instance();

    void reload(LoadedResource resource);

}

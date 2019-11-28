package xyz.ressor.service;

import org.jetbrains.annotations.Nullable;
import xyz.ressor.service.error.ErrorHandler;
import xyz.ressor.source.LoadedResource;
import xyz.ressor.source.SourceVersion;

public interface RessorService<T> {

    /**
     * Returns the original instance, which extends {@link RessorService} subclass, since actual service proxies don't
     * directly extend them.
     */
    RessorService<T> unwrap();

    /**
     * Returns the underlying type of the user service.
     */
    Class<? extends T> underlyingType();

    /**
     * Latest non-proxied instance of the user service.
     *
     * Important! This method is used implicitly by {@link xyz.ressor.service.proxy.ServiceProxyBuilder} while building
     * service proxy instance.
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
     * Error handler which should be used for this service in case of exceptions
     */
    ErrorHandler errorHandler();

    /**
     * Attempts to reload the service with the given resource.
     *
     * @param resource the new version of the resource
     * @return true if the resource was successfully applied, unless false
     */
    boolean reload(@Nullable LoadedResource resource);

    /**
     * Is service currently reloading.
     */
    boolean isReloading();

}

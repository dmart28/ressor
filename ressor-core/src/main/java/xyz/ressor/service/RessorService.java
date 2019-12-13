package xyz.ressor.service;

import org.jetbrains.annotations.Nullable;
import xyz.ressor.service.error.ErrorHandler;
import xyz.ressor.source.LoadedResource;
import xyz.ressor.source.ResourceId;
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
     * Latest non-proxied instance of the user service. Throws {@link IllegalStateException} if there is no underlying instance.
     *
     * Important! This method is used implicitly by {@link xyz.ressor.service.proxy.ServiceProxyBuilder} while building
     * service proxy instance.
     */
    T instance();

    /**
     * The resource identifier which this service operate.
     *
     * @return the resource id
     */
    ResourceId getResourceId();

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
     * If the service is already reloading from another thread, will return false.
     * In case of any error will throw appropriate exception.
     *
     * @param resource the new version of the resource
     * @param force whether to force the reload, blocking until concurrent reloads completed
     * @return true if the resource was reloaded, unless false
     */
    boolean reload(@Nullable LoadedResource resource, boolean force);

    default boolean reload(@Nullable LoadedResource resource) {
        return reload(resource, false);
    }

    /**
     * Is this service currently reloading.
     */
    boolean isReloading();

    /**
     * Same as {@link #instance()} but returning <b>null</b> instead of throwing an exception.
     */
    default T safeInstance() {
        try {
            return instance();
        } catch (IllegalStateException e) {
            return null;
        }
    }

}

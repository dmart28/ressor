package xyz.ressor.source;

/**
 * Represents the data source from which Ressor loads resources.
 */
public interface Source<R extends ResourceId> {

    /**
     * The unique identifier of the given source in the given Ressor context.
     *
     * @return the source ID
     */
    String id();

    /**
     * Loads the contents of the resource if it was modified since the provided version.
     *
     * @param version the last modified version prior which the resource shouldn't be loaded
     * @throws RuntimeException or its subclass in case of any error
     * @return the loaded resource or null
     */
    LoadedResource loadIfModified(R resourceId, SourceVersion version);

    /**
     * Describes whether you can subscribe for the changes on this resource.
     *
     * @return <b>true</b> if {@link Source#subscribe(ResourceId, Runnable)} call is supported, otherwise <b>false</b>
     */
    boolean isListenable();

    /**
     * Subscribe for the change events from the Source.
     *
     * @return subscription handle if {@link #isListenable()} <b>true</b>, otherwise <b>null</b>
     */
    Subscription subscribe(R resourceId, Runnable listener);

    /**
     * Returns description of the service in any format.
     */
    String describe();

    default SourceVersion emptyVersion() {
        return SourceVersion.EMPTY;
    }

    /**
     * Load resource forcibly from the {@link Source}.
     *
     * @return the loaded resource or null
     */
    default LoadedResource load(R resourceId) {
        return loadIfModified(resourceId, emptyVersion());
    }

}

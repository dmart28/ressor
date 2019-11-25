package xyz.ressor.source;

/**
 * Represents the data source
 */
public interface Source {

    /**
     * Loads the contents of the resource if it was modified since the provided version
     *
     * @param version the last modified version prior which the resource shouldn't be loaded
     * @throws RuntimeException or its subclass in case of any error
     * @return the loaded resource or null
     */
    LoadedResource loadIfModified(SourceVersion version);

    /**
     * Describes whether you can subscribe for the changes on this resource
     * @return true if {@link Source#subscribe(Runnable)} call is supported, otherwise false
     */
    boolean isListenable();

    /**
     * Subscribe for the change events from the Source
     */
    Subscription subscribe(Runnable listener);

    String describe();

    default SourceVersion emptyVersion() {
        return SourceVersion.EMPTY;
    }

    /**
     * Load resource forcibly from the {@link Source}
     * @return the loaded resource or null
     */
    default LoadedResource load() {
        return loadIfModified(emptyVersion());
    }

}

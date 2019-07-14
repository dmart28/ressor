package xyz.ressor.source;

import java.util.function.Consumer;

public interface Source {

    /**
     * Loads the contents of the resource if it was modified since the {@param lastModifiedMillis} time
     * @param lastModifiedMillis the last modified date prior which the resource shouldn't be loaded
     * @return the loaded resource or null
     */
    LoadedResource loadIfModified(long lastModifiedMillis);

    /**
     * Describes whether you can subscribe for the changes on this resource
     * @return true if {@link Source#subscribe(Consumer)} call is supported, otherwise false
     */
    boolean isListenable();

    void subscribe(Consumer<LoadedResource> listener);

    default LoadedResource load() {
        return loadIfModified(-1);
    }

}

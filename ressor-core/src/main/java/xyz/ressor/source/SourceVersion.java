package xyz.ressor.source;

import org.jetbrains.annotations.Nullable;

/**
 * Represents the version of resource loaded from the {@link Source}.
 * <p/>
 * Actual implementation depends on how the versioning is implemented by the source.
 */
public interface SourceVersion {

    /**
     * Actual version value
     * @param <V> value type
     * @return version value
     */
    @Nullable
    <V> V val();

    SourceVersion EMPTY = new SourceVersion() {
        @Override
        public <V> V val() {
            return null;
        }
    };

}

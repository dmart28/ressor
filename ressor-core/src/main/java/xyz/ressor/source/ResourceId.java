package xyz.ressor.source;

/**
 * Identifies the resource locations, which are then being loaded by {@link Source} implementations.
 *
 * For file system it's file, for Http it's URL, etc.
 */
public interface ResourceId {

    @Override
    String toString();

    Class<?> sourceType();

}

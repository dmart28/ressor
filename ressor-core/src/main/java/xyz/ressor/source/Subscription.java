package xyz.ressor.source;

/**
 * Subscription handle of the {@link Source}.
 */
public interface Subscription {

    /**
     * Unsubscribe previously registered listener from the underlying {@link Source}.
     */
    void unsubscribe();

}

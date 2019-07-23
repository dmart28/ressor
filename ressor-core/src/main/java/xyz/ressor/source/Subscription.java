package xyz.ressor.source;

public interface Subscription {

    /**
     * Unsubscribe previously registered listener from the {@link Source}
     */
    void unsubscribe();

}

package xyz.ressor.source;

public interface NonListenableSource<R extends ResourceId> extends Source<R> {

    @Override
    default Subscription subscribe(R resourceId, Runnable listener) {
        return null;
    }

    @Override
    default boolean isListenable() {
        return false;
    }
}

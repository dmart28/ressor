package xyz.ressor.source;

public interface NonListenableSource extends Source {

    default Subscription subscribe(Runnable listener) {
        return null;
    }

    default boolean isListenable() {
        return false;
    }
}

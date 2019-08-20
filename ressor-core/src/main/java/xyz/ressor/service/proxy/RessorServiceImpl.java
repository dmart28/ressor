package xyz.ressor.service.proxy;

import xyz.ressor.service.RessorService;
import xyz.ressor.source.LoadedResource;
import xyz.ressor.translator.Translator;

import java.io.InputStream;
import java.util.concurrent.locks.StampedLock;
import java.util.function.Function;

public class RessorServiceImpl<T> implements RessorService<T> {
    private final Function<Object, ? extends T> factory;
    private final Translator<InputStream, ?> translator;
    private final Class<? extends T> type;
    private T underlyingInstance;
    private long lastModifiedMillis;
    private final StampedLock lock = new StampedLock();

    public RessorServiceImpl(Class<? extends T> type, Function<Object, ? extends T> factory,
                             Translator<InputStream, ?> translator) {
        this.type = type;
        this.factory = factory;
        this.translator = translator;
    }

    @Override
    public Class<? extends T> underlyingType() {
        return type;
    }

    @Override
    public T instance() {
        var stamp = lock.tryOptimisticRead();
        var instance = underlyingInstance;
        if (!lock.validate(stamp)) {
            stamp = lock.readLock();
            try {
                instance = underlyingInstance;
            } finally {
                lock.unlockRead(stamp);
            }
        }
        return instance;
    }

    @Override
    public long lastModifiedMillis() {
        var stamp = lock.tryOptimisticRead();
        var result = lastModifiedMillis;
        if (!lock.validate(stamp)) {
            stamp = lock.readLock();
            try {
                result = lastModifiedMillis;
            } finally {
                lock.unlockRead(stamp);
            }
        }
        return result;
    }

    @Override
    public void reload(LoadedResource resource) {
        long stamp = lock.writeLock();
        try {
            this.lastModifiedMillis = resource.getLastModifiedMillis();
            this.underlyingInstance = factory.apply(translator.translate(resource.getInputStream()));
        } finally {
            lock.unlockWrite(stamp);
        }
    }
}

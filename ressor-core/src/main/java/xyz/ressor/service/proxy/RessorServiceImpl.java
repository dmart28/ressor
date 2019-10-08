package xyz.ressor.service.proxy;

import xyz.ressor.commons.utils.Exceptions;
import xyz.ressor.service.RessorService;
import xyz.ressor.source.LoadedResource;
import xyz.ressor.source.SourceVersion;
import xyz.ressor.translator.Translator;

import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.locks.StampedLock;
import java.util.function.Function;

import static xyz.ressor.commons.utils.RessorUtils.firstNonNull;

public class RessorServiceImpl<T> implements RessorService<T> {
    private final Function<Object, ? extends T> factory;
    private final Translator<InputStream, ?> translator;
    private final Class<? extends T> type;
    private T initialInstance;
    private T underlyingInstance;
    private SourceVersion latestVersion;
    private final Map<Object, Object> state = new HashMap<>();
    private final StampedLock lock = new StampedLock();

    public RessorServiceImpl(Class<? extends T> type, Function<Object, ? extends T> factory,
                             Translator<InputStream, ?> translator, T initialInstance) {
        this.type = type;
        this.factory = factory;
        this.translator = translator;
        this.initialInstance = initialInstance;
    }

    @Override
    public RessorService<T> unwrap() {
        return this;
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
        return firstNonNull(instance, initialInstance);
    }

    @Override
    public SourceVersion latestVersion() {
        var stamp = lock.tryOptimisticRead();
        var result = latestVersion;
        if (!lock.validate(stamp)) {
            stamp = lock.readLock();
            try {
                result = latestVersion;
            } finally {
                lock.unlockRead(stamp);
            }
        }
        return result;
    }

    @Override
    public void reload(LoadedResource resource) {
        if (resource != null) {
            var newResource = factory.apply(translator.translate(resource.getInputStream()));
            long stamp = lock.writeLock();
            try {
                this.latestVersion = resource.getVersion();
                this.underlyingInstance = newResource;
                try {
                    resource.getInputStream().close();
                } catch (Throwable ignored) {
                }
            } finally {
                lock.unlockWrite(stamp);
            }
        }
    }

    private boolean isEmpty(SourceVersion version) {
        return version == null || version == SourceVersion.EMPTY;
    }

    public <K, V> V state(K key) {
        return (V) state.get(key);
    }

    public RessorServiceImpl<T> state(Object key, Object value) {
        state.put(key, value);
        return this;
    }

}

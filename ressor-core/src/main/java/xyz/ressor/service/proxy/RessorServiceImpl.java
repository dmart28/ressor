package xyz.ressor.service.proxy;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import xyz.ressor.service.RessorService;
import xyz.ressor.service.error.ErrorHandler;
import xyz.ressor.source.LoadedResource;
import xyz.ressor.source.SourceVersion;
import xyz.ressor.translator.Translator;

import java.io.InputStream;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Function;

import static xyz.ressor.commons.utils.RessorUtils.firstNonNull;
import static xyz.ressor.commons.utils.RessorUtils.silentlyClose;

public class RessorServiceImpl<T> implements RessorService<T> {
    private static final Logger log = LoggerFactory.getLogger(RessorServiceImpl.class);
    private final Function<Object, ? extends T> factory;
    private final Translator<InputStream, ?> translator;
    private final ErrorHandler errorHandler;
    private final Class<? extends T> type;
    private final Map<Object, Object> state = new ConcurrentHashMap<>();
    private final T initialInstance;
    private volatile T underlyingInstance;
    private volatile SourceVersion latestVersion;
    private volatile AtomicBoolean isReloading = new AtomicBoolean(false);

    public RessorServiceImpl(Class<? extends T> type, Function<Object, ? extends T> factory,
                             Translator<InputStream, ?> translator, ErrorHandler errorHandler, T initialInstance) {
        this.type = type;
        this.factory = factory;
        this.translator = translator;
        this.errorHandler = errorHandler;
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
        var val = firstNonNull(underlyingInstance, initialInstance);
        if (val == null) {
            throw new IllegalStateException("The service wasn't loaded yet, please provide service initial instance.");
        }
        return val;
    }

    @Override
    public SourceVersion latestVersion() {
        return latestVersion;
    }

    @Override
    public boolean reload(LoadedResource resource) {
        if (resource != null) {
            if (!isReloading.compareAndExchange(false, true)) {
                try {
                    var newResource = factory.apply(translator.translate(resource.getInputStream()));
                    this.latestVersion = resource.getVersion();
                    this.underlyingInstance = newResource;
                    silentlyClose(resource.getInputStream());
                    return true;
                } finally {
                    isReloading.set(false);
                }
            } else {
                log.debug("Unable to reload service {}, since it's already reloading", type);
            }
        }
        return false;
    }

    @Override
    public boolean isReloading() {
        return isReloading.get();
    }

    @Override
    public ErrorHandler errorHandler() {
        return errorHandler;
    }

    public <K, V> V state(K key) {
        return (V) state.get(key);
    }

    public RessorServiceImpl<T> state(Object key, Object value) {
        if (key != null && value != null) {
            state.put(key, value);
        }
        return this;
    }

}

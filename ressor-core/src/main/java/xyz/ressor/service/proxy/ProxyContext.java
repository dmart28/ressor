package xyz.ressor.service.proxy;

import xyz.ressor.ext.ServiceExtension;
import xyz.ressor.service.error.ErrorHandler;
import xyz.ressor.source.Source;
import xyz.ressor.translator.Translator;

import java.io.InputStream;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.function.Function;

public class ProxyContext<T> {
    private final Class<T> type;
    private final Source source;
    private final Translator<InputStream, ?> translator;
    private final Function<Object, ? extends T> factory;
    private final List<ServiceExtension> extensions;
    private final ClassLoader classLoader;
    private final T initialInstance;
    private final Object[] proxyDefaultArguments;
    private final ErrorHandler errorHandler;

    public static <T> ProxyContextBuilder<T> builder(Class<T> type) {
        return new ProxyContextBuilder<>(type);
    }

    private ProxyContext(Class<T> type, Source source, Translator<InputStream, ?> translator,
                         Function<Object, ? extends T> factory, List<ServiceExtension> extensions,
                         ClassLoader classLoader, T initialInstance, Object[] proxyDefaultArguments,
                         ErrorHandler errorHandler) {
        this.type = type;
        this.source = source;
        this.translator = translator;
        this.factory = factory;
        this.extensions = extensions == null ? Collections.emptyList() : Collections.unmodifiableList(extensions);
        this.classLoader = classLoader;
        this.initialInstance = initialInstance;
        this.proxyDefaultArguments = proxyDefaultArguments;
        this.errorHandler = errorHandler;
    }

    public Class<T> getType() {
        return type;
    }

    public Source getSource() {
        return source;
    }

    public Translator<InputStream, ?> getTranslator() {
        return translator;
    }

    public Function<Object, ? extends T> getFactory() {
        return factory;
    }

    public List<ServiceExtension> getExtensions() {
        return extensions;
    }

    public ClassLoader getClassLoader() {
        return classLoader;
    }

    public T getInitialInstance() {
        return initialInstance;
    }

    public Object[] getProxyDefaultArguments() {
        return proxyDefaultArguments;
    }

    public ErrorHandler getErrorHandler() {
        return errorHandler;
    }

    public static class ProxyContextBuilder<T> {
        private final Class<T> type;
        private Source source;
        private Translator<InputStream, ?> translator;
        private Function<Object, ? extends T> factory;
        private List<ServiceExtension> extensions;
        private ClassLoader classLoader;
        private T initialInstance;
        private Object[] proxyDefaultArguments;
        private ErrorHandler errorHandler;

        private ProxyContextBuilder(Class<T> type) {
            this.type = type;
        }

        public ProxyContextBuilder<T> source(Source source) {
            this.source = source;
            return this;
        }

        public ProxyContextBuilder<T> translator(Translator<InputStream, ?> translator) {
            this.translator = translator;
            return this;
        }

        public <D> ProxyContextBuilder<T> factory(Function<D, ? extends T> factory) {
            this.factory = (Function<Object, ? extends T>) factory;
            return this;
        }

        public ProxyContextBuilder<T> addExtension(ServiceExtension extension) {
            if (extensions == null) {
                extensions = new LinkedList<>();
            }
            this.extensions.add(extension);
            return this;
        }

        public ProxyContextBuilder<T> classLoader(ClassLoader classLoader) {
            this.classLoader = classLoader;
            return this;
        }

        public ProxyContextBuilder<T> initialInstance(T initialInstance) {
            this.initialInstance = initialInstance;
            return this;
        }

        public ProxyContextBuilder<T> proxyDefaultArguments(Object... proxyDefaultArguments) {
            this.proxyDefaultArguments = proxyDefaultArguments;
            return this;
        }

        public ProxyContextBuilder<T> errorHandler(ErrorHandler errorHandler) {
            this.errorHandler = errorHandler;
            return this;
        }

        public ProxyContext<T> build() {
            return new ProxyContext<>(type, source, translator, factory, extensions, classLoader,
                    initialInstance, proxyDefaultArguments, errorHandler);
        }
    }
}

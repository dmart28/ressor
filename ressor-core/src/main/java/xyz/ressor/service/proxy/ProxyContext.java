package xyz.ressor.service.proxy;

import xyz.ressor.ext.ServiceExtension;
import xyz.ressor.service.error.ErrorHandler;
import xyz.ressor.source.ResourceId;
import xyz.ressor.source.Source;
import xyz.ressor.translator.Translator;

import java.io.InputStream;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.function.Function;

public class ProxyContext<T, D> {
    private final Class<T> type;
    private final Source source;
    private final ResourceId resource;
    private final Translator<InputStream, D> translator;
    private final Function<D, ? extends T> factory;
    private final List<ServiceExtension> extensions;
    private final ClassLoader classLoader;
    private final T initialInstance;
    private final Object[] proxyDefaultArguments;
    private final ErrorHandler errorHandler;
    private final boolean proxyObjectClassMethods;

    public static <T, D> ProxyContextBuilder<T, D> builder(Class<T> type) {
        return new ProxyContextBuilder<>(type);
    }

    private ProxyContext(Class<T> type, Source source, ResourceId resource, Translator<InputStream, D> translator,
                         Function<D, ? extends T> factory, List<ServiceExtension> extensions,
                         ClassLoader classLoader, T initialInstance, Object[] proxyDefaultArguments,
                         ErrorHandler errorHandler, boolean proxyObjectClassMethods) {
        this.type = type;
        this.source = source;
        this.resource = resource;
        this.translator = translator;
        this.factory = factory;
        this.extensions = extensions == null ? Collections.emptyList() : Collections.unmodifiableList(extensions);
        this.classLoader = classLoader;
        this.initialInstance = initialInstance;
        this.proxyDefaultArguments = proxyDefaultArguments;
        this.errorHandler = errorHandler;
        this.proxyObjectClassMethods = proxyObjectClassMethods;
    }

    public Class<T> getType() {
        return type;
    }

    public Source getSource() {
        return source;
    }

    public ResourceId getResource() {
        return resource;
    }

    public Translator<InputStream, D> getTranslator() {
        return translator;
    }

    public Function<D, ? extends T> getFactory() {
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

    public boolean isProxyObjectClassMethods() {
        return proxyObjectClassMethods;
    }

    public static class ProxyContextBuilder<T, D> {
        private final Class<T> type;
        private Source source;
        private Translator<InputStream, D> translator;
        private ResourceId resource;
        private Function<D, ? extends T> factory;
        private List<ServiceExtension> extensions;
        private ClassLoader classLoader;
        private T initialInstance;
        private Object[] proxyDefaultArguments;
        private ErrorHandler errorHandler;
        private boolean proxyObjectClassMethods = true;

        private ProxyContextBuilder(Class<T> type) {
            this.type = type;
        }

        public ProxyContextBuilder<T, D> source(Source source) {
            this.source = source;
            return this;
        }

        public ProxyContextBuilder<T, D> resource(ResourceId resource) {
            this.resource = resource;
            return this;
        }

        public ProxyContextBuilder<T, D> translator(Translator<InputStream, D> translator) {
            this.translator = translator;
            return this;
        }

        public ProxyContextBuilder<T, D> factory(Function<D, ? extends T> factory) {
            this.factory = factory;
            return this;
        }

        public ProxyContextBuilder<T, D> addExtension(ServiceExtension extension) {
            if (extensions == null) {
                extensions = new LinkedList<>();
            }
            this.extensions.add(extension);
            return this;
        }

        public ProxyContextBuilder<T, D> classLoader(ClassLoader classLoader) {
            this.classLoader = classLoader;
            return this;
        }

        public ProxyContextBuilder<T, D> initialInstance(T initialInstance) {
            this.initialInstance = initialInstance;
            return this;
        }

        public ProxyContextBuilder<T, D> proxyDefaultArguments(Object... proxyDefaultArguments) {
            this.proxyDefaultArguments = proxyDefaultArguments;
            return this;
        }

        public ProxyContextBuilder<T, D> errorHandler(ErrorHandler errorHandler) {
            this.errorHandler = errorHandler;
            return this;
        }

        public ProxyContextBuilder<T, D> proxyObjectClassMethods(boolean value) {
            this.proxyObjectClassMethods = value;
            return this;
        }

        public ProxyContext<T, D> build() {
            return new ProxyContext<>(type, source, resource, translator, factory, extensions, classLoader,
                    initialInstance, proxyDefaultArguments, errorHandler, proxyObjectClassMethods);
        }
    }
}

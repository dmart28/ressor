package xyz.ressor;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import xyz.ressor.commons.exceptions.RessorBuilderException;
import xyz.ressor.commons.utils.Exceptions;
import xyz.ressor.commons.watch.fs.FileSystemWatchService;
import xyz.ressor.ext.ServiceExtension;
import xyz.ressor.service.RessorService;
import xyz.ressor.service.proxy.ProxyContext;
import xyz.ressor.service.proxy.ServiceProxyBuilder;
import xyz.ressor.source.Source;
import xyz.ressor.source.fs.FileSystemSource;
import xyz.ressor.translator.Translator;
import xyz.ressor.translator.Translators;

import java.io.InputStream;
import java.nio.charset.Charset;
import java.nio.file.Path;
import java.util.LinkedList;
import java.util.concurrent.ForkJoinPool;
import java.util.function.Function;

import static java.nio.charset.StandardCharsets.UTF_8;

/**
 * The builder for the Ressor proxy class, which will be created and loaded by a ClassLoader.
 *
 *
 * If {@param <T>} is a class, it will be extended by our proxy class.
 * If {@param <T>} doesn't have a default constructor, Ressor will scan it for the mostly short and public constructor available, which will be used
 * for the proxy instance creation (which happens once).
 *
 * This can't be avoided since this is how the JVM inheritance mechanism works - we should call at least one constructor of super type, even though
 * we are creating an instance of our own new proxy class.
 *
 * Otherwise, you can mark your constructor with {@link xyz.ressor.commons.annotations.ProxyConstructor} annotation, which will tell Ressor
 * to use it explicitly. If constructor has parameters, Ressor will guess and pass the default ones, based on the underlying parameter type ({@link null} for objects,
 * 0 for ints, etc). Alternatively, you can provide your own param values with {@link #proxyDefaultArguments(Object...)}.
 *
 * By default, Ressor will also find the constructor to create the actual instances of your service. You can alternatively mark
 * the desired constructor/factory method with {@link xyz.ressor.commons.annotations.ServiceFactory} annotation. It must have a single parameter,
 * which will be of type of your selected {@link Translator} ({@link #yaml()}, {@link #json()}, etc).
 *
 *
 * If {@param <T>} is an interface, it will be implemented by our proxy class.
 * In that case you should provide a {@link #factory(Function)} which will be in charge of creating the actual instances of your service based
 * on the {@link Source} ({@link #yaml()}, {@link #json()}, etc).
 *
 * @param <T> service public type
 */
public class RessorBuilder<T> {
    private static final Logger log = LoggerFactory.getLogger(RessorBuilder.class);
    private static final ServiceProxyBuilder proxyBuilder = new ServiceProxyBuilder();
    private final Class<T> type;
    private Translator<InputStream, ?> translator;
    private Function<?, ? extends T> factory;
    private Source source;
    private FileSystemWatchService fsWatchService;
    private T initialValue;
    private boolean isAsync;
    private boolean gzipped = false;
    private ClassLoader classLoader;
    private LinkedList<ServiceExtension> extensions = new LinkedList<>();
    private Object[] proxyDefaultArguments;

    public RessorBuilder(Class<T> type) {
        this.type = type;
    }

    /**
     * Expect YAML data format from the source, will provide {@link com.fasterxml.jackson.databind.JsonNode} instance
     * to the service factory
     */
    public RessorBuilder<T> yaml() {
        this.translator = Translators.inputStream2Yaml();
        return this;
    }

    /**
     * Expect YAML data format from the source, will provide {@link com.fasterxml.jackson.core.JsonParser} instance
     * to the service factory
     */
    public RessorBuilder<T> yamlParser() {
        this.translator = Translators.inputStream2YamlParser();
        return this;
    }

    /**
     * Expect JSON data format from the source, will provide {@link com.fasterxml.jackson.databind.JsonNode} instance
     * to the service factory
     */
    public RessorBuilder<T> json() {
        this.translator = Translators.inputStream2Json();
        return this;
    }

    /**
     * Expect JSON data format from the source, will provide {@link com.fasterxml.jackson.core.JsonParser} instance
     * to the service factory
     */
    public RessorBuilder<T> jsonParser() {
        this.translator = Translators.inputStream2JsonParser();
        return this;
    }

    /**
     * Fetches the raw byte array from the source and pass it to the service factory
     */
    public RessorBuilder<T> bytes() {
        this.translator = Translators.inputStream2Bytes();
        return this;
    }

    /**
     * Read the source data as a single string and pass it to the service factory
     */
    public RessorBuilder<T> string() {
        return string(UTF_8);
    }

    /**
     * Read the source data as a single string and pass it to the service factory
     * @param charset the charset used to decode data
     */
    public RessorBuilder<T> string(Charset charset) {
        this.translator = Translators.inputStream2String(charset);
        return this;
    }

    /**
     * Read the source data as string lines (separated by System.lineSeparator) and pass it to the service factory
     */
    public RessorBuilder<T> lines() {
        return lines(UTF_8);
    }

    /**
     * Read the source data as string lines (separated by System.lineSeparator) and pass it to the service factory
     * @param charset the charset used to decode data
     */
    public RessorBuilder<T> lines(Charset charset) {
        this.translator = Translators.inputStream2Lines(charset);
        return this;
    }

    /**
     * Your custom data translator implementation. The resulting type of {@param translator} will be provided
     * to your service factory
     */
    public RessorBuilder<T> translator(Translator<InputStream, ?> translator) {
        this.translator = translator;
        return this;
    }

    /**
     * Your custom service factory. Service factory is responsible for creating your service instance,
     * accepting the resulting data generated by the {@link Translator}.
     */
    public <D> RessorBuilder<T> factory(Function<D, ? extends T> factory) {
        this.factory = factory;
        return this;
    }

    /**
     * Tells Ressor to use the given file as a data {@link Source}.
     *
     * @param resourcePath the FS path to the resource
     */
    public RessorBuilder<T> fileSource(String resourcePath) {
        if (fsWatchService == null) {
            fsWatchService = new FileSystemWatchService();
        }
        this.source = new FileSystemSource(resourcePath, fsWatchService);
        return this;
    }

    public RessorBuilder<T> fileSource(Path resourcePath) {
        return fileSource(resourcePath.toFile().getAbsolutePath());
    }

    public RessorBuilder<T> fileSource(FileSystemSource fileSource) {
        this.source = fileSource;
        return this;
    }

    /**
     * Provide your custom data {@param source} to the Ressor to use for your service
     */
    public RessorBuilder<T> source(Source source) {
        this.source = source;
        return this;
    }

    /**
     * Whether the source data is GZIP encoded
     */
    public RessorBuilder<T> gzipped() {
        this.gzipped = true;
        return this;
    }

    /**
     * The initial default instance of your service. It will be used by Ressor before the first
     * {@link Source#load()} is happened.
     *
     * Make sense only if {@link #asyncInitialReload()} is used.
     */
    public RessorBuilder<T> initialInstance(T initialValue) {
        this.initialValue = initialValue;
        return this;
    }

    /**
     * Whether to perform the initial data load asynchronously. Defaults to false.
     */
    public RessorBuilder<T> asyncInitialReload() {
        this.isAsync = true;
        return this;
    }

    /**
     * ClassLoader to use for loading the Ressor generated service proxy class
     */
    public RessorBuilder<T> classLoader(ClassLoader classLoader) {
        this.classLoader = classLoader;
        return this;
    }

    public RessorBuilder<T> addExtension(ServiceExtension extension) {
        this.extensions.add(extension);
        return this;
    }

    /**
     * The default arguments for the constructor, which service proxy class will be calling as
     * super(...)
     */
    public RessorBuilder<T> proxyDefaultArguments(Object... proxyDefaultArguments) {
        this.proxyDefaultArguments = proxyDefaultArguments;
        return this;
    }

    /**
     * Builds the Ressor service proxy instance. Along with building, it will also conduct the
     * initial data load using {@link Source#load()}, either sync or async.
     */
    public T build() {
        if (source == null) {
            throw new RessorBuilderException("No source instance provided");
        }
        if (translator == null) {
            throw new RessorBuilderException("The data format of the source is unknown, please provide a translator");
        }
        if (fsWatchService != null) {
            fsWatchService.init();
        }
        if (gzipped) {
            translator = Translators.gzipped(translator);
        }
        var ctx = ProxyContext.builder(type)
                .source(source)
                .classLoader(classLoader)
                .factory(factory)
                .proxyDefaultArguments(proxyDefaultArguments)
                .initialInstance(initialValue)
                .translator(translator);
        if (extensions.size() > 0) {
            extensions.forEach(ctx::addExtension);
        }
        var proxy = (RessorService<T>) proxyBuilder.buildProxy(ctx.build());
        if (isAsync) {
            Ressor.globals().threadPool().submit(() -> reload(proxy));
        } else {
            reload(proxy);
        }
        return (T) proxy;
    }

    private void reload(RessorService<T> proxy) {
        try {
            log.debug("Loading {} with initial instance from source [{}]", type, source.describe());
            proxy.reload(source.load());
        } catch (Throwable t) {
            if (isAsync) {
                log.error("Failed reloading service [{}] from the [{}] source: {}", type, source.describe(), t.getMessage(), t);
            } else {
                throw Exceptions.wrap(t);
            }
        }

    }

}

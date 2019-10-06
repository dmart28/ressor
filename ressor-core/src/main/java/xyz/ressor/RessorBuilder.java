package xyz.ressor;

import xyz.ressor.commons.exceptions.RessorBuilderException;
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
 * This can't be avoided since this is how the JVM inheritance mechanism works - we should call at least one constructor of super type, even though
 * we are creating an instance of our own new proxy class.
 * Otherwise, you can mark your constructor with {@link xyz.ressor.commons.annotations.ProxyConstructor} annotation, which will tell Ressor
 * to use it explicitly. If constructor has parameters, Ressor will guess and pass the default ones, based on the underlying parameter type ({@link null} for objects,
 * 0 for ints, etc). Alternatively, you can provide your own param values with {@link #proxyDefaultArguments(Object...)}.
 * By default, Ressor will also find the constructor for the creation of actual instances of your service. You can alternatively mark
 * the desired constructor/factory method with {@link xyz.ressor.commons.annotations.ServiceFactory} annotation. It must have single parameter,
 * which will be of type of your selected {@link Source} ({@link #yaml()}, {@link #json()}, etc).
 *
 *
 * If {@param <T>} is an interface, it will be implemented by our proxy class.
 * In that case you should provide a {@link #factory(Function)} which will be in charge of creating the actual instances of your service based
 * on the {@link Source} ({@link #yaml()}, {@link #json()}, etc).
 *
 * @param <T> service public type
 */
public class RessorBuilder<T> {
    private static final ServiceProxyBuilder proxyBuilder = new ServiceProxyBuilder();
    private final Class<T> type;
    private Translator<InputStream, ?> translator;
    private Function<?, ? extends T> factory;
    private Source source;
    private FileSystemWatchService fsWatchService;
    private T initialValue;
    private boolean isAsync;
    private ClassLoader classLoader;
    private LinkedList<ServiceExtension> extensions = new LinkedList<>();
    private Object[] proxyDefaultArguments;

    public RessorBuilder(Class<T> type) {
        this.type = type;
    }

    public RessorBuilder<T> yaml() {
        this.translator = Translators.inputStream2Yaml();
        return this;
    }

    public RessorBuilder<T> yamlParser() {
        this.translator = Translators.inputStream2YamlParser();
        return this;
    }

    public RessorBuilder<T> json() {
        this.translator = Translators.inputStream2Json();
        return this;
    }

    public RessorBuilder<T> jsonParser() {
        this.translator = Translators.inputStream2JsonParser();
        return this;
    }

    public RessorBuilder<T> bytes() {
        this.translator = Translators.inputStream2Bytes();
        return this;
    }

    public RessorBuilder<T> string() {
        return string(UTF_8);
    }

    public RessorBuilder<T> string(Charset charset) {
        this.translator = Translators.inputStream2String(charset);
        return this;
    }

    public RessorBuilder<T> lines() {
        return lines(UTF_8);
    }

    public RessorBuilder<T> lines(Charset charset) {
        this.translator = Translators.inputStream2Lines(charset);
        return this;
    }

    public RessorBuilder<T> translator(Translator<InputStream, ?> translator) {
        this.translator = translator;
        return this;
    }

    public <D> RessorBuilder<T> factory(Function<D, ? extends T> factory) {
        this.factory = factory;
        return this;
    }

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

    public RessorBuilder<T> source(Source source) {
        this.source = source;
        return this;
    }

    public RessorBuilder<T> initialInstance(T initialValue) {
        this.initialValue = initialValue;
        return this;
    }

    public RessorBuilder<T> asyncInitialReload() {
        this.isAsync = true;
        return this;
    }

    public RessorBuilder<T> classLoader(ClassLoader classLoader) {
        this.classLoader = classLoader;
        return this;
    }

    public RessorBuilder<T> addExtension(ServiceExtension extension) {
        this.extensions.add(extension);
        return this;
    }

    public RessorBuilder<T> proxyDefaultArguments(Object... proxyDefaultArguments) {
        this.proxyDefaultArguments = proxyDefaultArguments;
        return this;
    }

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
            ForkJoinPool.commonPool().submit(() -> reload(proxy));
        } else {
            reload(proxy);
        }
        return (T) proxy;
    }

    private void reload(RessorService<T> proxy) {
        proxy.reload(source.load());
    }

}

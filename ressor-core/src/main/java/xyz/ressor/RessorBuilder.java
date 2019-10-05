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

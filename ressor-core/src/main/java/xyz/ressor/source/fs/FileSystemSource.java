package xyz.ressor.source.fs;

import xyz.ressor.commons.watch.fs.FileSystemWatchService;
import xyz.ressor.source.LoadedResource;
import xyz.ressor.source.Source;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Consumer;

import static java.nio.file.Files.newInputStream;
import static xyz.ressor.commons.utils.Exceptions.wrap;

public class FileSystemSource implements Source {
    private static final String CLASSPATH_PREFIX = "classpath:";
    private final String rawResourcePath;
    private final Path resourcePath;
    private FileSystemWatchService watchService;
    private final boolean isClasspath;
    private final List<Consumer<LoadedResource>> listeners = new CopyOnWriteArrayList<>();
    private final AtomicBoolean isSubscribed = new AtomicBoolean();

    public FileSystemSource(String resourcePath) {
        this(resourcePath, null);
    }

    public FileSystemSource(String resourcePath, FileSystemWatchService watchService) {
        this.isClasspath = isClasspath(resourcePath);
        this.resourcePath = isClasspath ? null : Path.of(resourcePath);
        this.rawResourcePath = resourcePath.replaceFirst(CLASSPATH_PREFIX, "");
        this.watchService = watchService;
    }

    @Override
    public LoadedResource loadIfModified(long lastModifiedMillis) {
        try {
            if (!isClasspath) {
                var currentLastModified = Files.getLastModifiedTime(resourcePath).toMillis();
                if (currentLastModified > lastModifiedMillis) {
                    return new LoadedResource(newInputStream(resourcePath), currentLastModified, rawResourcePath);
                } else {
                    return null;
                }
            } else if (lastModifiedMillis < 0) {
                return new LoadedResource(getClass().getClassLoader().getResourceAsStream(rawResourcePath), -1, rawResourcePath);
            } else {
                return null;
            }
        } catch (Throwable t) {
            throw wrap(t);
        }
    }

    @Override
    public boolean isListenable() {
        return watchService != null;
    }

    @Override
    public void subscribe(Consumer<LoadedResource> listener) {
        listeners.add(listener);
        if (!isSubscribed.compareAndExchange(false, true)) {
            watchService.registerJob(resourcePath, p -> {
                listeners.forEach(l -> l.accept(loadIfModified(-1)));
            });
        }
    }

    public boolean isClasspath() {
        return isClasspath;
    }

    public Path getResourcePath() {
        return resourcePath;
    }

    private boolean isClasspath(String resourcePath) {
        return resourcePath.startsWith(CLASSPATH_PREFIX);
    }

}

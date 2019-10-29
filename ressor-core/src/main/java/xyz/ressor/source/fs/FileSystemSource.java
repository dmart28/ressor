package xyz.ressor.source.fs;

import xyz.ressor.commons.watch.fs.FileSystemWatchService;
import xyz.ressor.source.LoadedResource;
import xyz.ressor.source.Source;
import xyz.ressor.source.SourceVersion;
import xyz.ressor.source.Subscription;
import xyz.ressor.source.version.LastModified;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.Consumer;

import static java.nio.file.Files.newInputStream;
import static xyz.ressor.commons.utils.Exceptions.wrap;

public class FileSystemSource implements Source {
    private static final SourceVersion EMPTY = new LastModified(-1L);
    private static final String CLASSPATH_PREFIX = "classpath:";
    private final String rawResourcePath;
    private final Path resourcePath;
    private final boolean isClasspath;
    private final FileSystemWatchService watchService;
    private final List<Consumer<Path>> listeners = new CopyOnWriteArrayList<>();
    private volatile long classpathLastModified = -1;

    public FileSystemSource(Path resourcePath) {
        this(resourcePath.toString(), null);
    }

    public FileSystemSource(String resourcePath) {
        this(resourcePath, null);
    }

    public FileSystemSource(Path resourcePath, FileSystemWatchService watchService) {
        this(resourcePath.toString(), watchService);
    }

    public FileSystemSource(String resourcePath, FileSystemWatchService watchService) {
        this.isClasspath = isClasspath(resourcePath);
        this.resourcePath = isClasspath ? null : Path.of(resourcePath);
        this.rawResourcePath = resourcePath.replaceFirst(CLASSPATH_PREFIX, "");
        this.watchService = watchService;
    }

    @Override
    public LoadedResource loadIfModified(SourceVersion version) {
        final long lastModifiedMillis = version.val();
        try {
            if (!isClasspath) {
                var currentLastModified = Files.getLastModifiedTime(resourcePath).toMillis();
                if (currentLastModified > lastModifiedMillis) {
                    return new LoadedResource(newInputStream(resourcePath), new LastModified(currentLastModified), rawResourcePath);
                } else {
                    return null;
                }
            } else {
                var currentLastModified = classpathLastModified;
                if (currentLastModified < 0) {
                    var resourceURI = getClass().getClassLoader().getResource(rawResourcePath);
                    if (resourceURI != null) {
                        classpathLastModified = (currentLastModified = resourceURI.openConnection().getLastModified());
                    }
                }
                if (currentLastModified > lastModifiedMillis) {
                    return new LoadedResource(getClass().getClassLoader().getResourceAsStream(rawResourcePath),
                            new LastModified(currentLastModified), CLASSPATH_PREFIX + rawResourcePath);
                } else {
                    return null;
                }
            }
        } catch (Throwable t) {
            throw wrap(t);
        }
    }

    @Override
    public boolean isListenable() {
        return watchService != null && resourcePath != null;
    }

    @Override
    public Subscription subscribe(Runnable listener) {
        if (!isListenable()) {
            throw new UnsupportedOperationException("You can't listen to classpath resources!");
        }
        final Consumer<Path> l = p -> listener.run();
        listeners.add(l);
        watchService.registerJob(resourcePath, l);
        return () -> {
            if (listeners.remove(l)) {
                watchService.unregisterJob(resourcePath, l);
            }
        };
    }

    @Override
    public String describe() {
        return rawResourcePath;
    }

    @Override
    public SourceVersion emptyVersion() {
        return EMPTY;
    }

    public boolean isClasspath() {
        return isClasspath;
    }

    private boolean isClasspath(String resourcePath) {
        return resourcePath.startsWith(CLASSPATH_PREFIX);
    }

}

package xyz.ressor.source.fs;

import xyz.ressor.commons.watch.fs.FileSystemWatchService;
import xyz.ressor.source.*;
import xyz.ressor.source.version.LastModified;

import java.io.FileNotFoundException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.Consumer;

import static java.lang.String.format;
import static java.nio.file.Files.newInputStream;
import static xyz.ressor.commons.utils.Exceptions.wrap;

public class FileSystemSource extends AbstractSource<FileSystemResourceId> {
    private static final SourceVersion EMPTY = new LastModified(-1L);
    private final FileSystemWatchService watchService;
    private final List<Consumer<Path>> listeners = new CopyOnWriteArrayList<>();

    public FileSystemSource() {
        this(null);
    }

    public FileSystemSource(FileSystemWatchService watchService) {
        this.watchService = watchService;
    }

    @Override
    public LoadedResource loadIfModified(FileSystemResourceId resourceId, SourceVersion version) {
        final long lastModifiedMillis = version.val();
        try {
            if (!resourceId.isClasspath()) {
                var currentLastModified = Files.getLastModifiedTime(resourceId.getResourcePath()).toMillis();
                if (currentLastModified > lastModifiedMillis) {
                    return new LoadedResource(newInputStream(resourceId.getResourcePath()), new LastModified(currentLastModified), resourceId);
                } else {
                    return null;
                }
            } else {
                var currentLastModified = lastModifiedMillis;
                if (lastModifiedMillis < 0) {
                    var resourceURI = getClass().getClassLoader().getResource(resourceId.getRawResourcePath());
                    if (resourceURI != null) {
                        currentLastModified = resourceURI.openConnection().getLastModified();
                    } else {
                        throw new FileNotFoundException(format("Can't find %s file on classpath", resourceId.getRawResourcePath()));
                    }
                }
                if (currentLastModified > lastModifiedMillis) {
                    return new LoadedResource(getClass().getClassLoader().getResourceAsStream(resourceId.getRawResourcePath()),
                            new LastModified(currentLastModified), resourceId);
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
        return watchService != null;
    }

    @Override
    public Subscription subscribe(FileSystemResourceId resourceId, Runnable listener) {
        if (!isListenable() || resourceId.getResourcePath() == null) {
            throw new UnsupportedOperationException("You can't listen to classpath resources!");
        }
        final Consumer<Path> l = p -> listener.run();
        listeners.add(l);
        watchService.registerJob(resourceId.getResourcePath(), l);
        return () -> {
            if (listeners.remove(l)) {
                watchService.unregisterJob(resourceId.getResourcePath(), l);
            }
        };
    }

    @Override
    public String describe() {
        return "FS";
    }

    @Override
    public SourceVersion emptyVersion() {
        return EMPTY;
    }

}

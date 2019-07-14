package xyz.ressor.source.fs;

import xyz.ressor.source.LoadedResource;
import xyz.ressor.source.Source;

import java.io.IOException;
import java.nio.file.FileSystems;
import java.nio.file.Path;
import java.nio.file.WatchService;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.function.Consumer;

import static xyz.ressor.commons.utils.Exceptions.wrap;

public class FileSystemSource implements Source {

    private static final Executor executor = Executors.newSingleThreadExecutor();
    private final Path file;
    private final List<Consumer<LoadedResource>> listeners = new CopyOnWriteArrayList<>();

    public FileSystemSource(Path file) {
        this.file = file;
    }

    @Override
    public LoadedResource loadIfModified(long lastModifiedMillis) {
        return null;
    }

    @Override
    public boolean isListenable() {
        return /*watchService != null*/true;
    }

    @Override
    public synchronized void subscribe(Consumer<LoadedResource> listener) {
        listeners.add(listener);
        if (listeners.size() > 0) {

        }
    }


}

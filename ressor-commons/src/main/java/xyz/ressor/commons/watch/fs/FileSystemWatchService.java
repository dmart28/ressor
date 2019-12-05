package xyz.ressor.commons.watch.fs;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.file.*;
import java.util.Collections;
import java.util.Map;
import java.util.Set;
import java.util.WeakHashMap;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.function.Consumer;

import static java.nio.file.StandardWatchEventKinds.ENTRY_MODIFY;
import static xyz.ressor.commons.utils.Exceptions.wrap;

public class FileSystemWatchService {
    private static final Logger log = LoggerFactory.getLogger(FileSystemWatchService.class);
    private final WatchService watchService = getWatchService();
    private final Map<Path, Set<Consumer<Path>>> listeners = new ConcurrentHashMap<>();
    private final ExecutorService executor = Executors.newSingleThreadExecutor();
    private volatile boolean isClosed = false;

    public FileSystemWatchService init() {
        executor.submit(() -> {
            log.debug("Initializing new File System watch service.");

            try {
                WatchKey key;
                while ((key = watchService.take()) != null && !Thread.currentThread().isInterrupted()) {
                    for (WatchEvent<?> event : key.pollEvents()) {
                        Path path = (Path) event.context();
                        if (path != null) {
                            Path resolvedPath = ((Path) key.watchable()).resolve(path);
                            Set<Consumer<Path>> ls = listeners.get(resolvedPath);
                            if (ls != null) {
                                try {
                                    ls.forEach(l -> l.accept(resolvedPath));
                                } catch (Throwable t) {
                                    log.error("Error calling callback for path {}", path, t);
                                }
                            }
                        } else {
                            log.debug("Empty path found for event {} {}", event.kind(), event.count());
                        }
                    }
                    key.reset();
                }
                destroy();
            } catch (Throwable t) {
                log.error("fsWatch: {}", t.getMessage(), t);
            }
        });
        return this;
    }

    public void destroy() {
        try {
            if (!isClosed) {
                isClosed = true;
                log.debug("Destroying File System watch service ...");
                watchService.close();
                log.debug("File System watch service destroyed.");
                executor.shutdownNow();
            }
        } catch (Throwable t) {
            throw wrap(t);
        }
    }

    public void registerJob(Path path, Consumer<Path> listener) {
        Path watchPath = path;
        if (!Files.isDirectory(watchPath)) {
            watchPath = watchPath.getParent();
        }
        if (!isClosed) {
            try {
                watchPath.register(watchService, ENTRY_MODIFY);
            } catch (IOException e) {
                throw wrap(e);
            }
            listeners.computeIfAbsent(path.toAbsolutePath(), k -> Collections.newSetFromMap(new WeakHashMap<>()))
                    .add(listener);
        }
    }

    public void unregisterJob(Path path, Consumer<Path> listener) {
        listeners.compute(path.toAbsolutePath(), (p, l) -> {
           if (l != null) {
               l.remove(listener);
           }
           return l != null && l.size() == 0 ? null : l;
        });
    }

    private static java.nio.file.WatchService getWatchService() {
        java.nio.file.WatchService w = null;
        try {
            w = FileSystems.getDefault().newWatchService();
        } catch (IOException e) {
            log.warn("Unable to create new Watch Service, ");
        }
        return w;
    }
}

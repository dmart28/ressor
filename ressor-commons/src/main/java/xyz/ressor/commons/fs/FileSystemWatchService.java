package xyz.ressor.commons.fs;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.file.*;
import java.util.Map;
import java.util.concurrent.*;
import java.util.function.Consumer;

import static java.nio.file.StandardWatchEventKinds.ENTRY_MODIFY;
import static xyz.ressor.commons.utils.Exceptions.wrap;

public class FileSystemWatchService {
    private static final Logger log = LoggerFactory.getLogger(FileSystemWatchService.class);
    private final WatchService watchService = getWatchService();
    private final ExecutorService executor = Executors.newSingleThreadExecutor();
    private final Map<Path, Consumer<Path>> listeners = new ConcurrentHashMap<>();

    public void init() {
        executor.submit(() -> {
            log.debug("Initializing new File System watch service.");

            try {
                WatchKey key;
                while ((key = watchService.take()) != null && !Thread.currentThread().isInterrupted()) {
                    for (WatchEvent<?> event : key.pollEvents()) {
                        var path = (Path) event.context();
                        if (path != null) {
                            var resolvedPath = ((Path) key.watchable()).resolve(path);
                            var listener = listeners.get(resolvedPath);
                            if (listener != null) {
                                try {
                                    listener.accept(resolvedPath);
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
            } catch (Throwable t) {
                log.error("fsWatch: {}", t.getMessage(), t);
            }
        });
    }

    public void destroy() {
        try {
            log.debug("Destroying File System watch service ...");
            executor.shutdownNow();
            executor.awaitTermination(5, TimeUnit.SECONDS);
            watchService.close();
            log.debug("File System watch service destroyed.");
        } catch (Throwable t) {
            throw wrap(t);
        }
    }

    public void registerJob(Path path, Consumer<Path> listener) {
        var watchPath = path;
        if (!Files.isDirectory(watchPath)) {
            watchPath = watchPath.getParent();
        }
        if (!executor.isShutdown()) {
            try {
                watchPath.register(watchService, ENTRY_MODIFY);
            } catch (IOException e) {
                throw wrap(e);
            }
            listeners.put(path.toAbsolutePath(), listener);
        }
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

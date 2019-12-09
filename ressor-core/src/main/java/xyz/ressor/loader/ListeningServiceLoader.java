package xyz.ressor.loader;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import xyz.ressor.service.RessorService;
import xyz.ressor.source.Source;
import xyz.ressor.source.Subscription;

import java.util.concurrent.ExecutorService;

public class ListeningServiceLoader extends ServiceLoaderBase {
    private static final Logger log = LoggerFactory.getLogger(ListeningServiceLoader.class);
    private final Subscription subscription;

    public ListeningServiceLoader(RessorService service, Source source, ExecutorService threadPool,
                                  int reloadRetryMaxMillis) {
        super(service, source);
        if (!source.isListenable()) {
            throw new IllegalArgumentException("Service source doesn't support listening, use polling instead");
        }
        log.debug("Subscribing {} to [{}] source", service.underlyingType(), source.describe());
        this.subscription = source.subscribe(service.getResourceId(), () -> threadPool.submit(() -> {
            try {
                long timeToWait = 1000;
                var reloaded = false;
                do {
                    reloaded = reload();
                    if (!reloaded) {
                        log.debug("The service {} wasn't reloaded, trying again in {} seconds ...", service.underlyingType(), timeToWait / 1000);
                        Thread.sleep(timeToWait);
                        timeToWait *= 2;
                    }
                } while(!reloaded && timeToWait <= reloadRetryMaxMillis);
            } catch (Throwable t) {
                log.error("Failed reloading service [{}] from the [{}] source: {}", service.underlyingType(), source.describe(), t.getMessage(), t);
            }
        }));
    }

    @Override
    public void stop() {
        if (subscription != null) {
            subscription.unsubscribe();
        }
    }

    private boolean reload() {
        if (!service.isReloading()) {
            log.debug("Reloading by notification from [{}]", source.describe());
            return LoaderHelper.reload(service, source);
        } else {
            return false;
        }
    }

}

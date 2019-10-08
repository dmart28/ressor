package xyz.ressor.loader;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import xyz.ressor.config.RessorGlobals;
import xyz.ressor.service.RessorService;
import xyz.ressor.source.Source;
import xyz.ressor.source.Subscription;

public class ListeningServiceLoader extends ServiceLoaderBase {
    private static final Logger log = LoggerFactory.getLogger(ListeningServiceLoader.class);
    private final Subscription subscription;

    public ListeningServiceLoader(RessorService service, Source source) {
        super(service, source);
        if (!source.isListenable()) {
            throw new IllegalArgumentException("Service source doesn't support listening, use polling instead");
        }
        log.debug("Subscribing {} to [{}] source", service.underlyingType(), source.describe());
        this.subscription = source.subscribe(() -> RessorGlobals.getInstance().threadPool().submit(() -> {
            try {
                log.debug("Reloading by notification from [{}]", source.describe());
                synchronized (service) {
                    service.reload(source.load(), true);
                }
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

}

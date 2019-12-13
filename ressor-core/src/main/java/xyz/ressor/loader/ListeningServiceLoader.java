package xyz.ressor.loader;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import xyz.ressor.service.RessorService;
import xyz.ressor.service.ServiceManager;
import xyz.ressor.source.Source;
import xyz.ressor.source.Subscription;

public class ListeningServiceLoader extends ServiceLoaderBase {
    private static final Logger log = LoggerFactory.getLogger(ListeningServiceLoader.class);
    private final Subscription subscription;

    public ListeningServiceLoader(ServiceManager serviceManager, RessorService service, Source source) {
        super(service, source);
        if (!source.isListenable()) {
            throw new IllegalArgumentException("Service source doesn't support listening, use polling instead");
        }
        log.debug("{}: subscribing resource {} to {} source", service.underlyingType(), service.getResourceId(), source.describe());
        this.subscription = source.subscribe(service.getResourceId(), () -> {
            serviceManager.reloadAsync(service, source)
                    .whenComplete((result, t) -> {
                        if (t != null) {
                            log.error("Failed reloading service {} from [source: {}, resource: {}]: {}", service.underlyingType(), source.describe(), service.getResourceId(), t.getMessage(), t);
                        } else if (result) {
                            log.debug("Service {} reload completed.", service.underlyingType());
                        } else {
                            log.warn("Forced reload from [source: {}, resource: {}] failed for service {}", source.describe(), service.getResourceId(), service.underlyingType());
                        }
                    });
        });
    }

    @Override
    public void stop() {
        if (subscription != null) {
            subscription.unsubscribe();
        }
    }

}

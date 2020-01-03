package xyz.ressor.service.action;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import xyz.ressor.service.RessorService;

public class TriggerReloadAction extends ServiceBasedAction {
    private static final Logger log = LoggerFactory.getLogger(TriggerReloadAction.class);
    private final boolean isAsync;

    public TriggerReloadAction(RessorService<?> service, boolean isAsync) {
        super(service);
        this.isAsync = isAsync;
    }

    @Override
    public boolean perform(RessorService target) {
        if (isAsync) {
            getServiceManager().reloadAsync(getService(), getSource()).whenComplete((result, t) -> {
               if (t != null) {
                   log.error("Failed to reload service {} by trigger from [source: {}, resource: {}]: {}", getService().underlyingType(),
                           getSource().describe(), getService().getResourceId(), t.getMessage(), t);
               }
            });
            return true;
        } else {
            return getServiceManager().reload(getService(), getSource());
        }
    }
}

package xyz.ressor.service.action;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import xyz.ressor.config.RessorConfig;
import xyz.ressor.loader.LoaderHelper;
import xyz.ressor.service.RessorService;

public class TriggerReloadAction extends ServiceBasedAction {
    private static final Logger log = LoggerFactory.getLogger(TriggerReloadAction.class);
    private final boolean isAsync;

    public TriggerReloadAction(RessorService service, boolean isAsync) {
        super(service);
        this.isAsync = isAsync;
    }

    @Override
    public boolean perform(RessorConfig config, RessorService target) {
        if (isAsync) {
            config.threadPool().submit((Runnable) this::performReload);
            return true;
        } else {
            return performReload();
        }
    }

    private boolean performReload() {
        try {
            return LoaderHelper.reload(getService(), getSource());
        } catch (Throwable t) {
            log.error("Failed to reload service {} by trigger from [source: {}, resource: {}]: {}", getService().underlyingType(),
                    getSource().describe(), getService().getResourceId(), t.getMessage(), t);
            return false;
        }
    }
}

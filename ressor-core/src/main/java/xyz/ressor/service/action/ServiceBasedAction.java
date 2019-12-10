package xyz.ressor.service.action;

import xyz.ressor.service.ReloadAction;
import xyz.ressor.service.RessorService;
import xyz.ressor.service.proxy.StateHelper;
import xyz.ressor.source.Source;

public abstract class ServiceBasedAction implements ReloadAction {
    private final RessorService service;
    private final Source source;

    public RessorService getService() {
        return service;
    }

    public Source getSource() {
        return source;
    }

    public ServiceBasedAction(RessorService service) {
        this.service = service;
        this.source = StateHelper.getSource(service);
    }

}

package xyz.ressor.service.action;

import xyz.ressor.service.ReloadAction;
import xyz.ressor.service.RessorService;
import xyz.ressor.service.ServiceManager;
import xyz.ressor.service.proxy.StateHelper;
import xyz.ressor.source.Source;

import java.util.concurrent.ExecutorService;

public abstract class ServiceBasedAction implements ReloadAction {
    private final RessorService<?> service;
    private final Source source;
    private ServiceManager serviceManager;
    private ExecutorService executorService;

    public RessorService<?> getService() {
        return service;
    }

    public Source getSource() {
        return source;
    }

    public ServiceManager getServiceManager() {
        return serviceManager;
    }

    public void setServiceManager(ServiceManager serviceManager) {
        this.serviceManager = serviceManager;
    }

    public ExecutorService getExecutorService() {
        return executorService;
    }

    public void setExecutorService(ExecutorService executorService) {
        this.executorService = executorService;
    }

    public ServiceBasedAction(RessorService<?> service) {
        this.service = service;
        this.source = service != null ? StateHelper.getSource(service) : null;
    }

}

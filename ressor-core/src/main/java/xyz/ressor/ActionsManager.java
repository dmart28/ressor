package xyz.ressor;

import xyz.ressor.service.ReloadAction;
import xyz.ressor.service.ServiceManager;
import xyz.ressor.service.action.ServiceBasedAction;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ExecutorService;

import static xyz.ressor.Ressor.checkRessorService;
import static xyz.ressor.service.proxy.StateVariables.ACTIONS;

public class ActionsManager {
    private final ServiceManager serviceManager;
    private final ExecutorService threadPool;

    public ActionsManager(ServiceManager serviceManager, ExecutorService threadPool) {
        this.serviceManager = serviceManager;
        this.threadPool = threadPool;
    }

    /**
     * Performs the given action when the service reload is triggered, but not yet performed.
     * <p />
     * For the standard provided actions please see {@link xyz.ressor.service.action.Actions} class.
     *
     * @param service service which is going to be reloaded
     * @param action action which decides whether reload would happen or not
     */
    public synchronized void onReload(Object service, ReloadAction action) {
        checkRessorService(service, ressorService -> {
            var actions = (List<ReloadAction>) ressorService.state(ACTIONS);
            if (actions == null) {
                actions = new CopyOnWriteArrayList<>();
                ressorService.state(ACTIONS, actions);
            }
            if (action instanceof ServiceBasedAction) {
                var serviceAction = (ServiceBasedAction) action;
                serviceAction.setServiceManager(serviceManager);
                serviceAction.setExecutorService(threadPool);
            }
            actions.add(action);
            return null;
        });
    }

    /**
     * Resets all the actions previously registered on the service.
     */
    public synchronized void resetAll(Object service) {
        checkRessorService(service, ressorService -> {
            var actions = (List<ReloadAction>) ressorService.state(ACTIONS);
            if (actions != null) {
                actions.clear();
            }
            return null;
        });
    }

}

package xyz.ressor;

import xyz.ressor.config.RessorConfig;
import xyz.ressor.service.ReloadAction;
import xyz.ressor.service.action.InternalReloadAction;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import static xyz.ressor.Ressor.checkRessorService;
import static xyz.ressor.service.proxy.StateVariables.ACTIONS;

public class ActionsManager {
    private final RessorConfig config;

    public ActionsManager(RessorConfig config) {
        this.config = config;
    }

    /**
     * Performs the given action when the service reload is triggered, but not yet performed.
     *
     * @param service service which is going to be reloaded
     * @param action action which decides whether reload would happen or not
     */
    public synchronized void onReload(Object service, ReloadAction action) {
        checkRessorService(service, ressorService -> {
            List<InternalReloadAction> actions = (List<InternalReloadAction>) ressorService.state(ACTIONS);
            if (actions == null) {
                actions = new CopyOnWriteArrayList<>();
                ressorService.state(ACTIONS, actions);
            }
            actions.add(InternalReloadAction.from(action, config));
            return null;
        });
    }

    /**
     * Resets all the actions previously registered on the service.
     */
    public synchronized void resetAll(Object service) {
        checkRessorService(service, ressorService -> {
            List<InternalReloadAction> actions = (List<InternalReloadAction>) ressorService.state(ACTIONS);
            if (actions != null) {
                actions.clear();
            }
            return null;
        });
    }

}

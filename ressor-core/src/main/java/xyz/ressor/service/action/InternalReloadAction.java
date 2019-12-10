package xyz.ressor.service.action;

import xyz.ressor.config.RessorConfig;
import xyz.ressor.service.ReloadAction;
import xyz.ressor.service.RessorService;

public interface InternalReloadAction {

    boolean perform(RessorService target);

    static InternalReloadAction from(ReloadAction action, RessorConfig config) {
        return target -> action.perform(config, target);
    }

}

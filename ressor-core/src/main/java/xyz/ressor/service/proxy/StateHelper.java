package xyz.ressor.service.proxy;

import xyz.ressor.service.RessorService;
import xyz.ressor.source.Source;

public final class StateHelper {

    public static Source getSource(RessorService service) {
        return (Source) ((RessorServiceImpl) service.unwrap()).state(StateVariables.SOURCE);
    }

}

package xyz.ressor.service;

import xyz.ressor.config.RessorConfig;

public interface ReloadAction {

    /**
     * The action which should be performed on the target service prior to reload.
     * The result is decision, whether the reloading should be performed or not.
     * <p />
     * It's guaranteed that target service state (version) is constant during the action execution and
     * {@link RessorService#isReloading()} on target will always return <b>true</b>.
     * <p />
     * Please make sure that long running actions can slow down or even block the service reloading process,
     * which can have a negative impact on the overall system performance.
     * <p />
     * The implementations are not expected to throw any exceptions, only the resulting continuation flag.
     *
     * @param target target Ressor service which triggers this action
     * @return <b>true</b> if the reload operation can be continued, otherwise <b>false</b>
     */
    boolean perform(RessorConfig config, RessorService target);

}

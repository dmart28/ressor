package xyz.ressor.service.action;

import xyz.ressor.commons.utils.Exceptions;
import xyz.ressor.config.RessorConfig;
import xyz.ressor.service.ReloadAction;
import xyz.ressor.service.RessorService;
import xyz.ressor.source.SourceVersion;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Future;
import java.util.function.BiPredicate;

import static xyz.ressor.commons.utils.RessorUtils.safe;

public final class Actions {

    /**
     * Triggers the asynchronous reload of service when the target service is being reloaded and
     * returns <b>true</b> immediately, without waiting for result.
     *
     * Target service is provided in {@link ReloadAction#perform(RessorConfig, RessorService)} call.
     *
     * @param service the service which should be reloaded when target service is reloading
     * @return action
     */
    public static ReloadAction triggerReload(Object service) {
        return new TriggerReloadAction(checkRessorService(service), true);
    }

    /**
     * Triggers the synchronous reload of service, making the target service wait for its result.
     * If reload is successful, target service proceed with its own reload, otherwise it aborts.
     *
     * Target service is provided in {@link ReloadAction#perform(RessorConfig, RessorService)} call.
     *
     * @param service the service which should be reloaded when target service is reloading
     * @return action
     */
    public static ReloadAction triggerAndWaitReload(Object service) {
        return new TriggerReloadAction(checkRessorService(service), false);
    }

    /**
     * Aborts the reload of target service if the versionPredicate returns true.
     *
     * Target service is provided in {@link ReloadAction#perform(RessorConfig, RessorService)} call.
     *
     * @param service which version is being compared with the target service
     * @param versionPredicate predicate, which has target service current resource version on left and provided service resource version on right
     * @return action
     */
    public static ReloadAction abortIf(Object service, BiPredicate<SourceVersion, SourceVersion> versionPredicate) {
        return new VersionCheckAction(checkRessorService(service), versionPredicate);
    }

    /**
     * Runs the actions and combine the results with OR operator.
     *
     * @param actions actions to be performed
     * @return action
     */
    public static ReloadAction or(ReloadAction... actions) {
        return (config, target) -> {
            for (var action : actions) {
                if (action.perform(config, target)) {
                    return true;
                }
            }
            return false;
        };
    }

    /**
     * Runs the actions and combine the results with AND operator.
     *
     * @param actions actions to be performed
     * @return action
     */
    public static ReloadAction and(ReloadAction... actions) {
        return (config, target) -> {
            for (var action : actions) {
                if (!action.perform(config, target)) {
                    return false;
                }
            }
            return true;
        };
    }

    /**
     * Runs all the actions in parallel, wait for the results and combine them with AND operator.
     *
     * @param actions actions to be performed in parallel to each other
     * @return action
     */
    public static ReloadAction andParallel(ReloadAction... actions) {
        return (config, target) -> {
            var futures = runAsync(config, target, actions);
            try {
                for (var f : futures) {
                    var result = f.get();
                    if (result == null || !result) {
                        return false;
                    }
                }
                return true;
            } catch (Throwable t) {
                throw Exceptions.wrap(t);
            }
        };
    }

    /**
     * Runs all the actions in parallel, wait for the results and combine them with OR operator.
     *
     * @param actions actions to be performed in parallel to each other
     * @return action
     */
    public static ReloadAction orParallel(ReloadAction... actions) {
        return (config, target) -> {
            var futures = runAsync(config, target, actions);
            try {
                for (var f : futures) {
                    var result = f.get();
                    if (result != null && result) {
                        return true;
                    }
                }
                return false;
            } catch (Throwable t) {
                throw Exceptions.wrap(t);
            }
        };
    }

    private static List<Future<Boolean>> runAsync(RessorConfig config, RessorService target, ReloadAction[] actions) {
        var futures = new ArrayList<Future<Boolean>>(actions.length);
        for (var action : actions) {
            futures.add(config.threadPool().submit(safe(() -> action.perform(config, target), null)));
        }
        return futures;
    }

    private static RessorService checkRessorService(Object service) {
        if (service instanceof RessorService) {
            return (RessorService) service;
        } else {
            throw new IllegalArgumentException("Provided service is not generated with Ressor");
        }
    }

}
package xyz.ressor.service.action;

import xyz.ressor.commons.utils.Exceptions;
import xyz.ressor.service.ReloadAction;
import xyz.ressor.service.RessorService;
import xyz.ressor.source.SourceVersion;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.function.BiPredicate;
import java.util.function.Predicate;

import static xyz.ressor.commons.utils.RessorUtils.safe;

public final class Actions {

    /**
     * Triggers the asynchronous reload of the service when the target service is being reloaded and
     * returns <b>true</b> immediately, without waiting for the result.
     *
     * Target service is provided in {@link ReloadAction#perform(RessorService)} call.
     *
     * @param service the service which should be reloaded when the target service is reloading
     * @return action
     */
    public static ReloadAction triggerReload(Object service) {
        return new TriggerReloadAction(checkRessorService(service), true);
    }

    /**
     * Triggers synchronous reload of the service, making the target service wait for its result first.
     * If reload is successful, target service proceed with its own reload, otherwise it aborts.
     *
     * Target service is provided in {@link ReloadAction#perform(RessorService)} call.
     *
     * @param service the service which should be reloaded when the target service is reloading
     * @return action
     */
    public static ReloadAction triggerAndWaitReload(Object service) {
        return new TriggerReloadAction(checkRessorService(service), false);
    }

    /**
     * Aborts reload of the target service if the versionPredicate returns true.
     *
     * Target service is provided in {@link ReloadAction#perform(RessorService)} call.
     *
     * @param service which version is being compared with the target service
     * @param versionPredicate predicate, which has the target service latest version on left and the provided service latest version on right
     * @return action
     */
    public static ReloadAction abortIf(Object service, BiPredicate<SourceVersion, SourceVersion> versionPredicate) {
        return new VersionCheckAction(checkRessorService(service), versionPredicate);
    }

    public static <T> ReloadAction abortIf(Predicate<RessorService<T>> predicate) {
        return predicate.negate()::test;
    }

    /**
     * Runs the actions and combine the results with OR operator.
     *
     * @param actions actions to be performed
     * @return action
     */
    public static ReloadAction or(ReloadAction... actions) {
        return target -> {
            for (ReloadAction action : actions) {
                if (action.perform(target)) {
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
        return target -> {
            for (ReloadAction action : actions) {
                if (!action.perform(target)) {
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
        return new ExecutorServiceAction((threadPool, target) -> {
            List<Future<Boolean>> futures = runAsync(target, threadPool, actions);
            try {
                for (Future<Boolean> f : futures) {
                    Boolean result = f.get();
                    if (result == null || !result) {
                        return false;
                    }
                }
                return true;
            } catch (Throwable t) {
                throw Exceptions.wrap(t);
            }
        });
    }

    /**
     * Runs all the actions in parallel, wait for the results and combine them with OR operator.
     *
     * @param actions actions to be performed in parallel to each other
     * @return action
     */
    public static ReloadAction orParallel(ReloadAction... actions) {
        return new ExecutorServiceAction((threadPool, target) -> {
            List<Future<Boolean>> futures = runAsync(target, threadPool, actions);
            try {
                for (Future<Boolean> f : futures) {
                    Boolean result = f.get();
                    if (result != null && result) {
                        return true;
                    }
                }
                return false;
            } catch (Throwable t) {
                throw Exceptions.wrap(t);
            }
        });
    }

    private static List<Future<Boolean>> runAsync(RessorService target, ExecutorService threadPool, ReloadAction[] actions) {
                List<Future<Boolean>> futures = new ArrayList<>(actions.length);
        for (ReloadAction action : actions) {
            futures.add(threadPool.submit(safe(() -> action.perform(target), null)));
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

package xyz.ressor.service.action;

import xyz.ressor.service.RessorService;

import java.util.concurrent.ExecutorService;
import java.util.function.BiPredicate;

public class ExecutorServiceAction extends ServiceBasedAction {
    private final BiPredicate<ExecutorService, RessorService> action;

    public ExecutorServiceAction(BiPredicate<ExecutorService, RessorService> action) {
        super(null);
        this.action = action;
    }

    @Override
    public boolean perform(RessorService target) {
        return action.test(getExecutorService(), target);
    }
}

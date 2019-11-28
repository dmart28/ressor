package xyz.ressor;

import xyz.ressor.loader.QuartzManager;
import xyz.ressor.loader.QuartzServiceLoader;
import xyz.ressor.service.proxy.RessorServiceImpl;
import xyz.ressor.source.Source;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.TimeUnit;

import static xyz.ressor.service.proxy.StateVariables.LOADER;
import static xyz.ressor.service.proxy.StateVariables.SOURCE;

/**
 * This class is responsible for building polling-based service loaders.
 */
public class PollingBuilder {
    private final RessorServiceImpl service;
    private final ExecutorService threadPool;
    private final QuartzManager manager;

    public PollingBuilder(RessorServiceImpl service, ExecutorService threadPool, QuartzManager manager) {
        this.service = service;
        this.threadPool = threadPool;
        this.manager = manager;
    }

    /**
     * Poll the {@link Source} for the new version of data, if available, for every timeValue.
     *
     * @param timeValue the time amount between polling
     * @param unit the unit of time
     */
    public void every(int timeValue, TimeUnit unit) {
        var loader = new QuartzServiceLoader(service, (Source) service.state(SOURCE), threadPool, manager);
        loader.start(timeValue, unit);
        service.state(LOADER, loader);
    }

    /**
     * Poll the {@link Source} for the new version of data, if available, by Cron schedule.
     *
     * http://www.quartz-scheduler.org/documentation/quartz-2.3.0/tutorials/crontrigger.html
     *
     * See https://www.freeformatter.com/cron-expression-generator-quartz.html for handy expression building.
     *
     * @param expression the cron expression
     */
    public void cron(String expression) {
        var loader = new QuartzServiceLoader(service, (Source) service.state(SOURCE), threadPool, manager);
        loader.start(expression);
        service.state(LOADER, loader);
    }

}

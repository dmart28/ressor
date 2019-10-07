package xyz.ressor;

import xyz.ressor.loader.QuartzServiceLoader;
import xyz.ressor.service.proxy.RessorServiceImpl;
import xyz.ressor.source.Source;

import java.util.concurrent.TimeUnit;

import static xyz.ressor.service.proxy.StateVariables.LOADER;
import static xyz.ressor.service.proxy.StateVariables.SOURCE;

public class PollingBuilder {
    private final RessorServiceImpl service;

    public PollingBuilder(RessorServiceImpl service) {
        this.service = service;
    }

    /**
     * Poll the {@link Source} for the new version of data, if available, for every {@param timeValue}
     * @param timeValue
     * @param unit
     */
    public void every(int timeValue, TimeUnit unit) {
        var loader = new QuartzServiceLoader(service, (Source) service.state(SOURCE));
        loader.start(timeValue, unit);
        service.state(LOADER, loader);
    }

    /**
     * Poll the {@link Source} for the new version of data, if available, by Cron schedule.
     *
     * http://www.quartz-scheduler.org/documentation/quartz-2.3.0/tutorials/crontrigger.html
     *
     * See https://www.freeformatter.com/cron-expression-generator-quartz.html for handy expression building
     *
     * @param expression the cron expression
     */
    public void cron(String expression) {
        var loader = new QuartzServiceLoader(service, (Source) service.state(SOURCE));
        loader.start(expression);
        service.state(LOADER, loader);
    }

}

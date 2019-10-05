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

    public void every(int timeValue, TimeUnit unit) {
        var loader = new QuartzServiceLoader(service, (Source) service.state(SOURCE));
        loader.start(timeValue, unit);
        service.state(LOADER, loader);
    }

    public void cron(String expression) {
        var loader = new QuartzServiceLoader(service, (Source) service.state(SOURCE));
        loader.start(expression);
        service.state(LOADER, loader);
    }

}

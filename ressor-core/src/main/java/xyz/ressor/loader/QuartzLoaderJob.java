package xyz.ressor.loader;

import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.quartz.JobKey;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import xyz.ressor.config.RessorGlobals;
import xyz.ressor.service.proxy.RessorServiceImpl;
import xyz.ressor.source.Source;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class QuartzLoaderJob implements Job {
    private static final Logger log = LoggerFactory.getLogger(QuartzLoaderJob.class);

    @Override
    public void execute(JobExecutionContext ctx) throws JobExecutionException {
        try {
            var threadPool = RessorGlobals.getInstance().threadPool();
            var service = (RessorServiceImpl) ctx.getMergedJobDataMap().get(QuartzServiceLoader.SERVICE_KEY);
            var source = (Source) ctx.getMergedJobDataMap().get(QuartzServiceLoader.SOURCE_KEY);

            threadPool.submit(() -> {
                try {
                    synchronized (service) {
                        var resource = source.loadIfModified(service.latestVersion());
                        if (resource != null) {
                            service.reload(resource);
                        } else {
                            log.debug("{}: nothing to reload from [{}]", service.underlyingType(), source.describe());
                        }
                    }
                } catch (Throwable t) {
                    log.error("Failed reloading service [{}] from the [{}] source: {}", service.underlyingType(), source.describe(), t.getMessage(), t);
                }
            });
        } catch (Throwable t) {
            throw new JobExecutionException(t.getMessage(), t);
        }
    }
}

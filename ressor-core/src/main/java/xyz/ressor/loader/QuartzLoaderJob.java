package xyz.ressor.loader;

import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import xyz.ressor.config.RessorGlobals;
import xyz.ressor.service.proxy.RessorServiceImpl;
import xyz.ressor.source.Source;

public class QuartzLoaderJob implements Job {
    private static final Logger log = LoggerFactory.getLogger(QuartzLoaderJob.class);

    @Override
    public void execute(JobExecutionContext ctx) throws JobExecutionException {
        try {
            var threadPool = RessorGlobals.getInstance().threadPool();
            var service = (RessorServiceImpl) ctx.get(QuartzServiceLoader.SERVICE_KEY);
            var source = (Source) ctx.get(QuartzServiceLoader.SOURCE_KEY);

            threadPool.submit(() -> {
                try {
                    var resource = source.loadIfModified(service.currentVersion());
                    if (resource != null) {
                        service.reload(resource);
                    } else {
                        log.debug("{}: nothing to reload from [{}]", service.underlyingType(), source.describe());
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

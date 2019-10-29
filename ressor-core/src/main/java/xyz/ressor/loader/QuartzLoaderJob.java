package xyz.ressor.loader;

import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import xyz.ressor.service.proxy.RessorServiceImpl;
import xyz.ressor.source.Source;

import java.lang.ref.WeakReference;
import java.util.concurrent.ExecutorService;

public class QuartzLoaderJob implements Job {
    private static final Logger log = LoggerFactory.getLogger(QuartzLoaderJob.class);

    @Override
    public void execute(JobExecutionContext ctx) throws JobExecutionException {
        try {
            var threadPool = (ExecutorService) ctx.getMergedJobDataMap().get(QuartzServiceLoader.THREAD_POOL_KEY);
            var serviceR = (WeakReference<RessorServiceImpl>) ctx.getMergedJobDataMap().get(QuartzServiceLoader.SERVICE_KEY);
            var sourceR = (WeakReference<Source>) ctx.getMergedJobDataMap().get(QuartzServiceLoader.SOURCE_KEY);

            if (serviceR.get() == null) {
                ctx.getScheduler().deleteJob(ctx.getJobDetail().getKey());
            } else {
                final var service = serviceR.get();
                final var source = sourceR.get();

                threadPool.submit(() -> {
                    try {
                        if (!service.isReloading()) {
                            var resource = source.loadIfModified(service.latestVersion());
                            if (resource != null) {
                                service.reload(resource);
                            } else {
                                log.debug("{}: nothing to reload from [{}]", service.underlyingType(), source.describe());
                            }
                        } else {
                            log.debug("Service {} is already reloading, skipping until the next trigger execution ...", service.underlyingType());
                        }
                    } catch (Throwable t) {
                        log.error("Failed reloading service [{}] from the [{}] source: {}", service.underlyingType(), source.describe(), t.getMessage(), t);
                    }
                });
            }
        } catch (Throwable t) {
            throw new JobExecutionException(t.getMessage(), t);
        }
    }
}

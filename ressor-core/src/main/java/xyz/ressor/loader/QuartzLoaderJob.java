package xyz.ressor.loader;

import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import xyz.ressor.service.ServiceManager;
import xyz.ressor.service.proxy.RessorServiceImpl;
import xyz.ressor.source.Source;

import java.lang.ref.WeakReference;

public class QuartzLoaderJob implements Job {
    private static final Logger log = LoggerFactory.getLogger(QuartzLoaderJob.class);

    @Override
    public void execute(JobExecutionContext ctx) throws JobExecutionException {
        try {
            ServiceManager serviceManager = (ServiceManager) ctx.getMergedJobDataMap().get(QuartzServiceLoader.SERVICE_MANAGER_KEY);
            WeakReference<RessorServiceImpl> serviceR = (WeakReference<RessorServiceImpl>) ctx.getMergedJobDataMap().get(QuartzServiceLoader.SERVICE_KEY);
            WeakReference<Source> sourceR = (WeakReference<Source>) ctx.getMergedJobDataMap().get(QuartzServiceLoader.SOURCE_KEY);

            final RessorServiceImpl service = serviceR.get();
            final Source source = sourceR.get();

            if (service == null || source == null) {
                ctx.getScheduler().deleteJob(ctx.getJobDetail().getKey());
            } else {
                serviceManager.tryReloadAsync(service, source)
                        .whenComplete((result, t) -> {
                            if (t != null) {
                                log.error("Failed reloading service {} from [source: {}, resource: {}]: {}", service.underlyingType(), source.describe(), service.getResourceId(), t.getMessage(), t);
                            } else if (!result) {
                                log.debug("Service {} is already reloading, skipping until the next trigger execution ...", service.underlyingType());
                            } else {
                                log.debug("Service {} reload completed.", service.underlyingType());
                            }
                        });
            }
        } catch (Throwable t) {
            throw new JobExecutionException(t.getMessage(), t);
        }
    }
}

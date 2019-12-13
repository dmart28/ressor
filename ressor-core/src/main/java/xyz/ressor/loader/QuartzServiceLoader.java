package xyz.ressor.loader;

import org.quartz.JobDataMap;
import org.quartz.JobKey;
import org.quartz.SchedulerException;
import org.quartz.Trigger;
import xyz.ressor.commons.utils.Exceptions;
import xyz.ressor.service.RessorService;
import xyz.ressor.service.ServiceManager;
import xyz.ressor.source.Source;

import java.lang.ref.WeakReference;
import java.time.ZoneOffset;
import java.util.TimeZone;
import java.util.concurrent.TimeUnit;

import static org.quartz.CronScheduleBuilder.cronSchedule;
import static org.quartz.JobBuilder.newJob;
import static org.quartz.SimpleScheduleBuilder.simpleSchedule;
import static org.quartz.TriggerBuilder.newTrigger;

public class QuartzServiceLoader extends ServiceLoaderBase {
    private static final TimeZone UTC = TimeZone.getTimeZone(ZoneOffset.UTC);
    static final String SERVICE_MANAGER_KEY = "__smk";
    static final String SERVICE_KEY = "__rs";
    static final String SOURCE_KEY = "__sk";
    private final ServiceManager serviceManager;
    private final QuartzManager quartzManager;
    private JobKey jobKey;

    public QuartzServiceLoader(RessorService service, Source source, ServiceManager serviceManager,
                               QuartzManager quartzManager) {
        super(service, source);
        this.serviceManager = serviceManager;
        this.quartzManager = quartzManager;
    }

    public void start(String expression) {
        var trigger = newTrigger()
                .withSchedule(cronSchedule(expression).inTimeZone(UTC))
                .build();
        start(trigger);
    }

    public void start(int every, TimeUnit unit) {
        var trigger = newTrigger()
                .withSchedule(simpleSchedule()
                        .withIntervalInMilliseconds(unit.toMillis(every))
                        .repeatForever())
                .build();
        start(trigger);
    }

    private void start(Trigger trigger) {
        var scheduler = quartzManager.scheduler();
        var dataMap = new JobDataMap();
        dataMap.put(SERVICE_MANAGER_KEY, serviceManager);
        dataMap.put(SERVICE_KEY, new WeakReference<>(service));
        dataMap.put(SOURCE_KEY, new WeakReference<>(source));
        var job = newJob(QuartzLoaderJob.class)
                .usingJobData(dataMap)
                .build();
        try {
            scheduler.scheduleJob(job, trigger);
            this.jobKey = job.getKey();
        } catch (SchedulerException e) {
            throw Exceptions.wrap(e);
        }
    }

    @Override
    public void stop() {
        if (jobKey != null) {
            try {
                quartzManager.scheduler().deleteJob(jobKey);
            } catch (SchedulerException e) {
                throw Exceptions.wrap(e);
            }
        }
    }
}

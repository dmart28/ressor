package xyz.ressor.loader;

import org.quartz.*;
import xyz.ressor.commons.utils.Exceptions;
import xyz.ressor.service.RessorService;
import xyz.ressor.source.Source;

import java.lang.ref.WeakReference;
import java.time.ZoneOffset;
import java.util.TimeZone;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.TimeUnit;

import static org.quartz.CronScheduleBuilder.cronSchedule;
import static org.quartz.JobBuilder.newJob;
import static org.quartz.SimpleScheduleBuilder.simpleSchedule;
import static org.quartz.TriggerBuilder.newTrigger;

public class QuartzServiceLoader extends ServiceLoaderBase {
    private static final TimeZone UTC = TimeZone.getTimeZone(ZoneOffset.UTC);
    static final String THREAD_POOL_KEY = "__tpk";
    static final String SERVICE_KEY = "__rs";
    static final String SOURCE_KEY = "__sk";
    private final QuartzManager manager;
    private final ExecutorService threadPool;
    private JobKey jobKey;

    public QuartzServiceLoader(RessorService service, Source source, ExecutorService threadPool,
                               QuartzManager manager) {
        super(service, source);
        this.threadPool = threadPool;
        this.manager = manager;
    }

    public void start(String expression) {
        Trigger trigger = newTrigger()
                .withSchedule(cronSchedule(expression).inTimeZone(UTC))
                .build();
        start(trigger);
    }

    public void start(int every, TimeUnit unit) {
        Trigger trigger = newTrigger()
                .withSchedule(simpleSchedule()
                        .withIntervalInMilliseconds(unit.toMillis(every))
                        .repeatForever())
                .build();
        start(trigger);
    }

    private void start(Trigger trigger) {
        Scheduler scheduler = manager.scheduler();
        JobDataMap dataMap = new JobDataMap();
        dataMap.put(THREAD_POOL_KEY, threadPool);
        dataMap.put(SERVICE_KEY, new WeakReference<>(service));
        dataMap.put(SOURCE_KEY, new WeakReference<>(source));
        JobDetail job = newJob(QuartzLoaderJob.class)
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
                manager.scheduler().deleteJob(jobKey);
            } catch (SchedulerException e) {
                throw Exceptions.wrap(e);
            }
        }
    }
}

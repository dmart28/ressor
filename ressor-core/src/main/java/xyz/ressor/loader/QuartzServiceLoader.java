package xyz.ressor.loader;

import org.quartz.*;
import xyz.ressor.commons.utils.Exceptions;
import xyz.ressor.service.RessorService;
import xyz.ressor.source.Source;

import java.time.ZoneOffset;
import java.util.TimeZone;
import java.util.concurrent.TimeUnit;

import static org.quartz.CronScheduleBuilder.cronSchedule;
import static org.quartz.JobBuilder.newJob;
import static org.quartz.SimpleScheduleBuilder.simpleSchedule;
import static org.quartz.TriggerBuilder.newTrigger;

public class QuartzServiceLoader extends ServiceLoaderBase {
    private static final TimeZone UTC = TimeZone.getTimeZone(ZoneOffset.UTC);
    protected static final String SERVICE_KEY = "__rs";
    protected static final String SOURCE_KEY = "__sk";
    private JobKey jobKey;

    public QuartzServiceLoader(RessorService service, Source source) {
        super(service, source);
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
        var scheduler = QuartzManager.getInstance().scheduler();
        var dataMap = new JobDataMap();
        dataMap.put(SERVICE_KEY, service);
        dataMap.put(SOURCE_KEY, source);
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
                QuartzManager.getInstance().scheduler().deleteJob(jobKey);
            } catch (SchedulerException e) {
                throw Exceptions.wrap(e);
            }
        }
    }
}

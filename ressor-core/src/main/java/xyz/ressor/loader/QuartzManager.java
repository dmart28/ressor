package xyz.ressor.loader;

import org.quartz.Scheduler;
import org.quartz.impl.StdSchedulerFactory;
import org.quartz.simpl.RAMJobStore;
import xyz.ressor.commons.utils.Exceptions;

import java.util.Properties;

public class QuartzManager {
    private final Scheduler scheduler;

    public QuartzManager(int pollingThreads) {
        try {
            StdSchedulerFactory schedulerFactory = new StdSchedulerFactory();
            Properties props = new Properties();
            props.setProperty(StdSchedulerFactory.PROP_SCHED_INSTANCE_NAME, "RessorScheduler");
            props.setProperty(StdSchedulerFactory.PROP_THREAD_POOL_PREFIX + ".threadCount", Integer.toString(pollingThreads));
            props.setProperty(StdSchedulerFactory.PROP_JOB_STORE_CLASS, RAMJobStore.class.getName());
            schedulerFactory.initialize(props);
            Scheduler scheduler = schedulerFactory.getScheduler();
            scheduler.start();

            this.scheduler = scheduler;
        } catch (Throwable t) {
            throw Exceptions.wrap(t);
        }
    }

    public QuartzManager(Scheduler scheduler) {
        this.scheduler = scheduler;
    }

    public Scheduler scheduler() {
        return scheduler;
    }

}

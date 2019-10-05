package xyz.ressor.loader;

import org.quartz.Scheduler;
import org.quartz.impl.StdSchedulerFactory;
import org.quartz.simpl.RAMJobStore;
import xyz.ressor.commons.utils.Exceptions;
import xyz.ressor.config.RessorGlobals;

import java.util.Properties;

public class QuartzManager {
    private static final Object lock = new Object();
    private static volatile QuartzManager instance;
    private final Scheduler scheduler;

    private QuartzManager(Scheduler scheduler) {
        this.scheduler = scheduler;
    }

    public Scheduler scheduler() {
        return scheduler;
    }

    public static QuartzManager getInstance() {
        if (instance == null) {
            synchronized (lock) {
                if (instance == null) {
                    try {
                        var schedulerFactory = new StdSchedulerFactory();
                        var props = new Properties();
                        props.setProperty(StdSchedulerFactory.PROP_SCHED_INSTANCE_NAME, "RessorScheduler");
                        props.setProperty(StdSchedulerFactory.PROP_THREAD_POOL_PREFIX + ".threadCount",
                                Integer.toString(RessorGlobals.getInstance().pollingThreads()));
                        props.setProperty(StdSchedulerFactory.PROP_JOB_STORE_CLASS, RAMJobStore.class.getName());
                        schedulerFactory.initialize(props);
                        var scheduler = schedulerFactory.getScheduler();
                        scheduler.start();

                        instance = new QuartzManager(scheduler);
                    } catch (Throwable t) {
                        throw Exceptions.wrap(t);
                    }
                }
            }
        }
        return instance;
    }

}

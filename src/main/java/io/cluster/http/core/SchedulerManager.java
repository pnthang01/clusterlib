/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package io.cluster.http.core;

import io.cluster.http.annotation.Scheduler;
import io.cluster.http.annotation.SchedulerUnit;
import io.cluster.util.ClassLoaderUtil;
import io.cluster.util.ShutdownHookCleanUp;
import io.cluster.util.StringUtil;
import java.io.IOException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 *
 * @author thangpham
 */
public class SchedulerManager {

    private static final Logger LOGGER = LogManager.getLogger(SchedulerManager.class.getName());

    private ScheduledExecutorService executorService;

    private static SchedulerManager _instance;

    public synchronized static SchedulerManager load() {
        if (null == _instance) {
            throw new NullPointerException("Scheduler Manager has not been initialized.");
        }
        return _instance;
    }

    public static void initialize(int maxThread, List<String> packages) throws ClassNotFoundException, IOException {
        List<Class<?>> allClasses = new ArrayList();
        for (String pkg : packages) {
            allClasses.addAll(ClassLoaderUtil.getClassesForPackage(pkg));
        }
        _instance = new SchedulerManager(maxThread, allClasses);
    }

    public static void initialize(int maxThread, String... packages) throws ClassNotFoundException, IOException {
        List<Class<?>> allClasses = new ArrayList();
        for (String pkg : packages) {
            allClasses.addAll(ClassLoaderUtil.getClassesForPackage(pkg));
        }
        _instance = new SchedulerManager(maxThread, allClasses);
    }

    public SchedulerManager(int maxThread, List<Class<?>> classes) {
        LOGGER.info("Initialize total " + classes.size() + " schedulers. Start to mapping...");
        executorService = Executors.newScheduledThreadPool(maxThread);
        ShutdownHookCleanUp shutdownCleanup = ShutdownHookCleanUp.load();
        shutdownCleanup.addExecutor(new ShutdownHookCleanUp.ExecutorCleanUpUnit("scheduler-manager", executorService));
        Calendar cal = Calendar.getInstance();
        Date currTime = cal.getTime();
        for (Class clazz : classes) {
            try {
                Scheduler anno = (Scheduler) clazz.getAnnotation(Scheduler.class);
                if (null != anno) {
                    AbstractScheduler newInstance = (AbstractScheduler) clazz.newInstance();
                    for (Method method : clazz.getDeclaredMethods()) {
                        SchedulerUnit methodAnno = method.getAnnotation(SchedulerUnit.class);
                        if (null != methodAnno) {
                            String scheduleName = methodAnno.name();
                            long delay = methodAnno.delay();
                            long period = methodAnno.period();
                            TimeUnit tu = methodAnno.timeUnit();
                            int type = methodAnno.type();
                            String schedulePattern = methodAnno.schedulePattern();
                            LOGGER.info("Follwing scheduler is mapped: " + scheduleName + " delay=" + delay
                                    + " period=" + period + " TimeUnit=" + tu.name() + " runType=" + type + " schedulePattern=" + schedulePattern);
                            SchedulerRunnable methodMapping = new SchedulerRunnable(newInstance, method);
                            if (schedulePattern == null) {
                                if (SchedulerUnit.FIX_RATE_TYPE == type) {
                                    executorService.scheduleAtFixedRate(methodMapping, delay, period, tu);
                                } else {
                                    executorService.scheduleWithFixedDelay(methodMapping, delay, period, tu);
                                }
                            } else {
                                cal = Calendar.getInstance();
                                tu = TimeUnit.SECONDS;
                                String[] split = schedulePattern.split(":");
                                if (split.length == 1) {
                                    cal.set(Calendar.SECOND, StringUtil.safeParseInt(split[0]));
                                    period = 60;
                                    if (cal.getTime().compareTo(currTime) <= 0) {
                                        cal.add(Calendar.MINUTE, 1);
                                    }
                                    delay = (cal.getTimeInMillis() - currTime.getTime()) / 1000;
                                }
                                if (split.length == 2) {
                                    cal.set(Calendar.MINUTE, StringUtil.safeParseInt(split[0]));
                                    cal.set(Calendar.SECOND, StringUtil.safeParseInt(split[1]));
                                    period = 3600;
                                    if (cal.getTime().compareTo(currTime) <= 0) {
                                        cal.add(Calendar.HOUR, 1);
                                    }
                                    delay = (cal.getTimeInMillis() - currTime.getTime()) / 1000;
                                }
                                if (split.length == 3) {
                                    cal.set(Calendar.HOUR_OF_DAY, StringUtil.safeParseInt(split[0]));
                                    cal.set(Calendar.MINUTE, StringUtil.safeParseInt(split[1]));
                                    cal.set(Calendar.SECOND, StringUtil.safeParseInt(split[2]));
                                    period = 86400;
                                    if (cal.getTime().compareTo(currTime) <= 0) {
                                        cal.add(Calendar.DATE, 1);
                                    }
                                    delay = (cal.getTimeInMillis() - currTime.getTime()) / 1000;
                                }
                                executorService.scheduleAtFixedRate(methodMapping, delay, period, tu);
                            }
                        }
                    }
                }
            } catch (Exception ex) {
                LOGGER.error("Cannot auto mapping to class: " + clazz.getSimpleName() + " with error");
            }
        }
    }

    private class SchedulerRunnable implements Runnable {

        private final AbstractScheduler scheduler;
        private final Method method;

        public SchedulerRunnable(AbstractScheduler scheduler, Method method) {
            this.scheduler = scheduler;
            this.method = method;
        }

        @Override
        public void run() {
            try {
                method.invoke(scheduler);
            } catch (Exception ex) {
                LOGGER.error("Error when invoke scheduler", ex);
            }
        }

    }
}

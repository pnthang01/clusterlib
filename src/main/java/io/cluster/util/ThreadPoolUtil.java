/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package io.cluster.util;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 *
 * @author thangpham
 */
public class ThreadPoolUtil {

    private static ThreadPoolUtil _instance;
    private ScheduledExecutorService threadPool;

    public ThreadPoolUtil() {
        threadPool = Executors.newScheduledThreadPool(20);
        ShutdownHookCleanUp shutdownHook = ShutdownHookCleanUp.load();
        shutdownHook.addExecutor(new ShutdownHookCleanUp.ExecutorCleanUpUnit("ThreadPoolUtil", threadPool));
    }

    public static synchronized ThreadPoolUtil load() {
        if (null == _instance) {
            _instance = new ThreadPoolUtil();
        }
        return _instance;
    }

    public void addThread(Runnable runnable, long delay, long period, TimeUnit tu) {
        if (period < 0) {
            threadPool.schedule(runnable, delay, tu);
        } else {
            threadPool.scheduleWithFixedDelay(runnable, delay, delay, tu);
        }
    }

}

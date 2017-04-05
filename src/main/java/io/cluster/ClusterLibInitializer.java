/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package io.cluster;

import io.cluster.config.ApplicationConfig;
import io.cluster.http.HttpLogServer;
import io.cluster.http.core.SchedulerManager;
import io.cluster.node.ServerNode;
import io.cluster.node.ClientNode;
import io.cluster.util.Constants;
import io.cluster.util.ShutdownHookCleanUp;
import io.cluster.util.StringUtil;
import io.cluster.util.ThreadPoolUtil;
import java.io.IOException;
import java.util.concurrent.TimeUnit;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 *
 * @author thangpham
 */
public class ClusterLibInitializer implements Runnable {

    static final Logger LOGGER = LogManager.getLogger(ClusterLibInitializer.class.getName());
    private ApplicationConfig appConfig = null;
    private ThreadPoolUtil threadPool = null;
    private ShutdownHookCleanUp shutdownCU = null;
    private boolean isConnect = false;
    
    public static ClusterLibInitializer initialize(String configPath) throws IOException {
        return new ClusterLibInitializer(configPath);
    }
    
    public boolean isConnect() {
        return isConnect;
    }

    public ClusterLibInitializer(String configPath) throws IOException {
        if (!StringUtil.isNullOrEmpty(configPath)) {
            Constants.setBaseConfigFolder(configPath);
        }
        initLog4J(Constants.getLog4JConfigFile());
        shutdownCU = ShutdownHookCleanUp.load();
        appConfig = ApplicationConfig.load();
        threadPool = ThreadPoolUtil.load();
        switch (appConfig.clusterType()) {
            case ApplicationConfig.SERVER_TYPE:
                isConnect = true;
                ServerNode.initialize();
                break;
            case ApplicationConfig.CLIENT_TYPE:
                isConnect = true;
                ClientNode.initialize();
                break;
            case ApplicationConfig.NORMAL_TYPE:
                ClientNode.initialize(false);
                break;
        }

        if (appConfig.enableHttp()) {
            enableHttpModule();
        }
        //
        if (appConfig.enableScheduler()) {
            enableScheduleModule();
        }
    }

    private void initLog4J(String path) throws IOException {
        /*Initialize log4j */
        System.setProperty("log4j.configurationFile", path);
        ((org.apache.logging.log4j.core.LoggerContext) LogManager.getContext(false)).reconfigure();
    }

    private void enableHttpModule() {
        HttpLogServer httpLogServer = new HttpLogServer(appConfig.httpHost(), appConfig.httpPort(), appConfig.httpLoadedControllerPackages());
        threadPool.addThread(httpLogServer, 300, -1, TimeUnit.MILLISECONDS);
        shutdownCU.addService(httpLogServer);
    }

    private void enableScheduleModule() {
        try {
            SchedulerManager.initialize(appConfig.schedulerMaxThread(), appConfig.schedulerLoadedJobPackages());
        } catch (ClassNotFoundException | IOException ex) {
            LOGGER.error("Could not initialize scheduler manager", ex);
        }
    }

    @Override
    public void run() {
        while (true) {
            try {
                LOGGER.info(String.format("FreeMemory %f M.B, UsedMemory: %f M.B, TotalMemory: %f M.B",
                        (double) (Runtime.getRuntime().freeMemory() / 1048576),
                        (double) ((Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory()) / 1048576),
                        (double) (Runtime.getRuntime().totalMemory() / 1048576)));
                Thread.sleep(600000);
            } catch (InterruptedException ex) {
                LOGGER.error("Could not sleep for a while. Why ?");
            }
        }
    }
}

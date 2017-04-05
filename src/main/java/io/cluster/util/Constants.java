package io.cluster.util;

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
/**
 *
 * @author thangpham
 */
public class Constants {

    public static class Channel {

        public static final String ALL_CHANNEL = "c_all";
        public static final String SYSTEM_CHANNEL = "c_system";
        public static final String NODE_CHANNEL = "c_node";
        public static final String TASK_CHANNEL = "c_task";
        public static final String COORDINATOR_CHANNEL = "c_coor";
        public static final String SUPERVISE_CHANNEL = "c_sup";
    }

    public static class Group {

        public static final String NONE_GROUP = "g_none";
        public static final String TASK_GROUP = "g_task";
        public static final String COORDINATOR_GROUP = "g_coor";
    }

    public static class Action {

        public static final String START_ACTION = "_start";
        public static final String RUN_ACTION = "_run";
        public static final String STOP_ACTION = "_stop";
        public static final String REGISTRY_ACTION = "_registry";
        public static final String REPORT_ACTION = "_report";
        public static final String NOTIFY_ACTION = "_notify";
        public static final String EDIT_ACTION = "_edit";
    }

    public static class Status {

        public static final String WAIT_STATUS = "WAIT";
        public static final String RUNNING_STATUS = "RUNNING";
        public static final String COMPLETED_STATUS = "COMPLETED";
        public static final String RETRYING_STATUS = "RETRYING";
        public static final String ERROR_STATUS = "ERROR";
        public static final String STOP_STATUS = "STOP";
        public static final String START_STATUS = "START";
        public static final String SILENCE_STATUS = "SILENCE";
    }

    private static final String LOG4J_CONFIGURATION_FILE = "log4j2.xml";
    private static final String APPLICATION_CONFIGURATION_FILE = "configuration.properties";
    private static String BASE_CONFIGURATION_FOLDER = "configs/";

    public static void setBaseConfigFolder(String folder) {
        BASE_CONFIGURATION_FOLDER = folder;
    }

    public static String getBaseConfigFolder() {
        return BASE_CONFIGURATION_FOLDER;
    }

    public static String getLog4JConfigFile() {
        return StringUtil.toString(BASE_CONFIGURATION_FOLDER, LOG4J_CONFIGURATION_FILE);
    }

    public static String getApplicationConfigFile() {
        return StringUtil.toString(BASE_CONFIGURATION_FOLDER, APPLICATION_CONFIGURATION_FILE);
    }

}

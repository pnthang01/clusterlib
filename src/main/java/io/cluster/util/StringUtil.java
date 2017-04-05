/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package io.cluster.util;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import io.cluster.model.TaskModel;
import java.lang.reflect.Type;
import java.net.SocketAddress;
import java.util.Map;

/**
 *
 * @author thangpham
 */
public class StringUtil {

    private static final Type MAP_JSON_TYPE = new TypeToken<Map<String, String>>() {
    }.getType();

    public static Map<String, String> fromJsonToMap(String json) {
        return gson.fromJson(json, MAP_JSON_TYPE);
    }

    private static final Gson gson = new Gson();

    public static <T> T fromJson(String json, Class<T> clazz) {
        return gson.fromJson(json, clazz);
    }

    public static <T> T fromJson(String json, Type type) {
        return gson.fromJson(json, type);
    }

    public static String toJson(Object o) {
        return gson.toJson(o);
    }

    public static String[] parseAddress(SocketAddress address) {
        String addressStr = address.toString();
        if (addressStr.startsWith("/")) {
            addressStr = addressStr.replace("/", "");
        }
        String[] tmp = addressStr.split(":");
        return tmp;
    }

    public static String toString(Object... args) {
        StringBuilder s = new StringBuilder();
        for (Object arg : args) {
            if (arg != null) {
                s.append(arg);
            }
        }
        return s.toString();
    }

    /**
     * Get nodeId by host and port
     *
     * @param host
     * @param port
     * @return
     */
    public static String getHashAddress(String host, int port) {
        return host + ":" + port;
    }

    public static String buildTaskId(TaskModel taskBean) {
        String taskId;
        if (StringUtil.isNullOrEmpty(taskBean.getTaskName())) {
            taskId = taskBean.getTaskName() + "_" + DateTimeUtil.formatISOTime(taskBean.getSubmitedTimed());
        } else {
            taskId = HashUtil.md5(taskBean.getHost() + ":" + taskBean.getPort()) + "_" + DateTimeUtil.formatISOTime(taskBean.getSubmitedTimed());
        }
        return taskId;
    }

    public static boolean safeParseBoolean(Object o) {
        return safeParseBoolean(o, false);
    }

    public static boolean safeParseBoolean(Object o, boolean defaultVal) {
        if (isNullOrEmpty(o)) {
            return defaultVal;
        }
        boolean n = defaultVal;
        try {
            n = Boolean.parseBoolean(o.toString());
        } catch (Throwable e) {
        }
        return n;
    }

    /**
     * Safe Parse Int
     *
     * @param s
     * @return
     */
    public static int safeParseInt(Object s) {
        return safeParseInt(s.toString(), 0);
    }

    public static int safeParseInt(Object s, int defaultVal) {
        if (isNullOrEmpty(s)) {
            return defaultVal;
        }
        int n = defaultVal;
        try {
            n = Integer.parseInt(s.toString());
        } catch (Throwable e) {
        }
        return n;
    }

    /**
     * Safe Parse Long
     *
     * @param s
     * @return
     */
    public static long safeParseLong(Object s) {
        return safeParseInt(s.toString(), 0);
    }

    public static long safeParseLong(Object s, long defaultVal) {
        if (isNullOrEmpty(s)) {
            return defaultVal;
        }
        long n = defaultVal;
        try {
            n = Long.parseLong(s.toString());
        } catch (Throwable e) {
        }
        return n;
    }

    /**
     * Safe Parse Double
     *
     * @param s
     * @return
     */
    public static double safeParseDouble(Object s) {
        return safeParseInt(s.toString(), 0);
    }

    public static double safeParseDouble(Object s, double defaultVal) {
        if (isNullOrEmpty(s)) {
            return defaultVal;
        }
        double n = defaultVal;
        try {
            n = Double.parseDouble(s.toString());
        } catch (Throwable e) {
        }
        return n;
    }

    public static boolean isNullOrEmpty(Object o) {
        return null == o || "".equals(o);
    }

    public static boolean isNullOrEmpty(String s) {
        return s == null || "".equals(s);
    }

}

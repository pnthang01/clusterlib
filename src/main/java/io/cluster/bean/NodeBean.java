/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package io.cluster.bean;

import io.cluster.util.MethodUtil;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 *
 * @author thangpham
 */
public class NodeBean {

    public static int RUNNING = 1, OVERLOAD = 2, TIMEOUT = 0, DIED = -1;

    private String group;
    private String id;
    private String name;
    private String host;
    private int port;
    private long startedTime;
    private long lastPingTime;
    private int status; //1 = Is running; 2 = Overload; 0 = Timeout; -1 = Died
    private ConcurrentMap<String, String> state;

    public NodeBean() {
        this.startedTime = System.currentTimeMillis();
        this.lastPingTime = System.currentTimeMillis();
        this.status = 1;
        this.state = new ConcurrentHashMap<>();
    }

    public NodeBean(String group, String id, String name, String host, int port) {
        this.group = group;
        this.id = id;
        this.name = name;
        this.host = host;
        this.port = port;
        this.startedTime = System.currentTimeMillis();
        this.lastPingTime = System.currentTimeMillis();
        this.status = 1;
        this.state = new ConcurrentHashMap<>();
    }

    public NodeBean(String id, String name, String host, int port) {
        this.id = id;
        this.name = name;
        this.host = host;
        this.port = port;
        this.startedTime = System.currentTimeMillis();
        this.lastPingTime = System.currentTimeMillis();
        this.status = 1;
        this.state = new ConcurrentHashMap<>();
    }

    @Override
    public String toString() {
        return MethodUtil.toJson(this);
    }

    public ConcurrentMap<String, String> getState() {
        return state;
    }

    public void setState(Map<String, String> state) {
        this.state.putAll(state);
    }

    public String getGroup() {
        return group;
    }

    public void setGroup(String group) {
        this.group = group;
    }

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public long getLastPingTime() {
        return lastPingTime;
    }

    public void ping() {
        this.lastPingTime = System.currentTimeMillis();
        this.status = 1;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getHost() {
        return host;
    }

    public void setHost(String host) {
        this.host = host;
    }

    public int getPort() {
        return port;
    }

    public void setPort(int port) {
        this.port = port;
    }

    public long getStartedTime() {
        return startedTime;
    }

    public void setStartedTime(long startedTime) {
        this.startedTime = startedTime;
    }

}

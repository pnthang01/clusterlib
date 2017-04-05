/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package io.cluster.model;

import java.lang.reflect.Field;
import java.util.Map;

/**
 *
 * @author thangpham
 */
public class TaskModel {

    private Map<String, String> instructions;
    private Boolean onNewProcess;
    private String host;
    private String taskName;
    private Integer port;
    private Integer parallelProcess;
    private Long delay;
    private Long period;
    private Long submitedTimed;
    private String status;
    private String env;
    private String taskId;
    private String messageReport;

    public int updateValue(TaskModel other) throws IllegalArgumentException, IllegalAccessException {
        if (null == other) {
            return 0;
        }
        int count = 0;
        for (Field field : TaskModel.class.getDeclaredFields()) {
            field.setAccessible(true);
            Object currValue = field.get(this);
            Object otherValue = field.get(other);
            if (otherValue != null && !currValue.equals(otherValue)) {
                field.set(this, otherValue);
                count++;
            }
        }
        return count;
    }

    public String getMessageReport() {
        return messageReport;
    }

    public TaskModel setMessageReport(String messageReport) {
        this.messageReport = messageReport;
        return this;
    }
    

    public String getStatus() {
        return status;
    }

    public TaskModel setStatus(String status) {
        this.status = status;
        return this;
    }

    public String getTaskId() {
        return taskId;
    }

    public TaskModel setTaskId(String taskId) {
        this.taskId = taskId;
        return this;
    }

    public String getTaskName() {
        return taskName;
    }

    public TaskModel setTaskName(String taskName) {
        this.taskName = taskName;
        return this;
    }

    public String getHost() {
        return host;
    }

    public String getEnv() {
        return env;
    }

    public TaskModel setEnv(String env) {
        this.env = env;
        return this;
    }

    public TaskModel setHost(String host) {
        this.host = host;
        return this;
    }

    public int getPort() {
        return port;
    }

    public TaskModel setPort(int port) {
        this.port = port;
        return this;
    }

    public boolean isOnNewProcess() {
        return onNewProcess;
    }

    public TaskModel setOnNewProcess(boolean onNewProcess) {
        this.onNewProcess = onNewProcess;
        return this;
    }

    public Map<String, String> getInstructions() {
        return instructions;
    }

    public TaskModel setInstructions(Map<String, String> instructions) {
        this.instructions = instructions;
        return this;
    }

    public int getParallelProcess() {
        return parallelProcess;
    }

    public TaskModel setParallelProcess(int parallelProcess) {
        this.parallelProcess = parallelProcess;
        return this;
    }

    public long getDelay() {
        return delay;
    }

    public TaskModel setDelay(long delay) {
        this.delay = delay;
        return this;
    }

    public long getPeriod() {
        return period;
    }

    public TaskModel setPeriod(long period) {
        this.period = period;
        return this;
    }

    public long getSubmitedTimed() {
        return submitedTimed;
    }

    public TaskModel setSubmitedTimed(long submitedTimed) {
        this.submitedTimed = submitedTimed;
        return this;
    }

}

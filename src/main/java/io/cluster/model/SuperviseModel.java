/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package io.cluster.model;

import java.io.Serializable;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

/**
 *
 * @author thangpham
 */
@Entity(name = "Supervise")
@Table( name = "supervise" )
public class SuperviseModel implements Serializable {

    @Id
    @Column(name = "process_id")
    private String processId;
    @Column(name = "timeout")
    private long timeout;
    @Column(name = "last_updated")
    private long lastUpdated = System.currentTimeMillis();
    @Column(name = "started_time")
    private long startedTime = System.currentTimeMillis();
    @Column(name = "process_name")
    private String processName;
    @Column(name = "status")
    private String status;

    public String getProcessId() {
        return processId;
    }

    public SuperviseModel setProcessId(String processId) {
        this.processId = processId;
        return this;
    }

    public long getTimeout() {
        return timeout;
    }

    public SuperviseModel setTimeout(long timeout) {
        this.timeout = timeout;
        return this;
    }

    public long getLastUpdated() {
        return lastUpdated;
    }

    public SuperviseModel setLastUpdated(long lastUpdated) {
        this.lastUpdated = lastUpdated;
        return this;
    }

    public long getStartedTime() {
        return startedTime;
    }

    public SuperviseModel setStartedTime(long startedTime) {
        this.startedTime = startedTime;
        return this;
    }

    public String getProcessName() {
        return processName;
    }

    public SuperviseModel setProcessName(String processName) {
        this.processName = processName;
        return this;
    }

    public String getStatus() {
        return status;
    }

    public SuperviseModel setStatus(String status) {
        this.status = status;
        this.lastUpdated = System.currentTimeMillis();
        return this;
    }

}

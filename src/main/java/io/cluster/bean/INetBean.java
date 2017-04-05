/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package io.cluster.bean;

import java.net.SocketAddress;

/**
 *
 * @author thangpham
 */
public abstract class INetBean {

    protected SocketAddress address;
    protected byte[] message;
    protected long loggedtime;

    public INetBean(SocketAddress address, byte... message) {
        this.address = address;
        this.message = message;
        this.loggedtime = System.currentTimeMillis();
    }

    public String getMessageAsString() {
        return message == null || message.length == 0 ? null : new String(message, 0, message.length).trim();
    }

    public SocketAddress getAddress() {
        return address;
    }

    public void setAddress(SocketAddress address) {
        this.address = address;
    }

    public byte[] getMessage() {
        return message;
    }

    public void setMessage(byte[] message) {
        this.message = message;
    }

    public long getLoggedtime() {
        return loggedtime;
    }

    public void setLoggedtime(long loggedtime) {
        this.loggedtime = loggedtime;
    }

}

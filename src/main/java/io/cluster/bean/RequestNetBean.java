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
public class RequestNetBean extends INetBean {

    private int port = -1;
    private String host;

    public RequestNetBean(SocketAddress address, byte... message) {
        super(address, message);
    }

    public String getHost() {
        if (null != host) {
            return host;
        }
        String address = this.address.toString();
        if (address.startsWith("/")) {
            address = address.replace("/", "");
        }
        String[] tmp = address.split(":");
        host = tmp[0];
        port = Integer.valueOf(tmp[1]);
        return host;
    }

    public int getPort() {
        if (port != -1) {
            return port;
        }
        String address = this.address.toString();
        if (address.startsWith("/")) {
            address = address.replace("/", "");
        }
        String[] tmp = address.split(":");
        host = tmp[0];
        port = Integer.valueOf(tmp[1]);
        return port;
    }

}

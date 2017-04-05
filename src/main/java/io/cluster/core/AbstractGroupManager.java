/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package io.cluster.core;

import io.cluster.bean.NodeBean;
import io.cluster.node.ServerNode;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 *
 * @author thangpham
 * @param <T>
 */
public abstract class AbstractGroupManager<T extends IMessageListener> {

    private static final Logger LOGGER = LogManager.getLogger(AbstractGroupManager.class.getName());

    protected final ConcurrentMap<String, NodeBean> nodeGroup;
    protected final ServerNode serverNode;

    public AbstractGroupManager() {
        this.serverNode = ServerNode.load();
        this.nodeGroup = new ConcurrentHashMap<>();
    }

    public void addNodeBean(NodeBean nodeBean) {
        nodeGroup.put(nodeBean.getId(), nodeBean);
    }

    public void removeNodeBean(NodeBean nodeBean) {
        nodeGroup.remove(nodeBean.getId(), nodeBean);
    }

    public void removeNodeBean(String hashId) {
        nodeGroup.remove(hashId);
    }

}

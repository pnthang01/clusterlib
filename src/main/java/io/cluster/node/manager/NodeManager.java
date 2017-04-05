/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package io.cluster.node.manager;

import io.cluster.bean.NodeBean;
import io.cluster.core.AbstractGroupManager;
import io.cluster.listener.ServerNodeMessageListener;
import io.cluster.util.Constants;
import io.cluster.util.StringUtil;
import io.cluster.util.ThreadPoolUtil;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 *
 * @author thangpham
 */
public class NodeManager extends AbstractGroupManager<ServerNodeMessageListener> {

    private static final Logger LOGGER = LogManager.getLogger(NodeManager.class.getName());

    private static NodeManager _instance;
    private final ConcurrentMap<String, ConcurrentMap<String, NodeBean>> groupedNodeMap;
    private final ConcurrentMap<String, AbstractGroupManager> managers;

    private final AtomicReference<NodeBean> newNode = new AtomicReference<>();
    private final Lock nodeLocker = new ReentrantLock(true);
    private final Condition newNodeCondition = nodeLocker.newCondition();

    private void init() {
        serverNode.addListener(Constants.Channel.NODE_CHANNEL, new ServerNodeMessageListener());
        //TODO: will change to configuration
        managers.put(Constants.Group.TASK_GROUP, TaskManager.load());
        managers.put(Constants.Group.COORDINATOR_GROUP, CoordinatorManager.load());
        ThreadPoolUtil threadPool = ThreadPoolUtil.load();
        //
        threadPool.addThread(() -> {
            for (Entry<String, ConcurrentMap<String, NodeBean>> nodeMap : groupedNodeMap.entrySet()) {
                for (Map.Entry<String, NodeBean> nodeEntry : nodeMap.getValue().entrySet()) {
                    NodeBean node = nodeEntry.getValue();
                    if (System.currentTimeMillis() - node.getLastPingTime() > 10000) {
                        node.setStatus(NodeBean.TIMEOUT);
                    }
                    if (System.currentTimeMillis() - node.getLastPingTime() > 60000) {
                        node.setStatus(NodeBean.DIED);
                    }
                }
            }
        }, 5, 5, TimeUnit.SECONDS);
    }

    public static synchronized NodeManager load() throws NullPointerException {
        if (null == _instance) {
            LOGGER.info("This NodeManager has not initialized yet. Create new...");
            _instance = new NodeManager();
            _instance.init();
        }
        return _instance;
    }

    public NodeManager() {
        this.groupedNodeMap = new ConcurrentHashMap<>();
        this.managers = new ConcurrentHashMap<>();
    }

    public NodeBean waitingForNewNode() {
        NodeBean returnNode = null;
        nodeLocker.lock();
        try {
            while (newNode.get() == null) {
                newNodeCondition.await();
            }
            returnNode = newNode.get();
            newNode.set(null);
        } catch (Exception ex) {
            LOGGER.error("Could not wait for new node to cluster, error: ", ex);
        } finally {
            nodeLocker.unlock();
        }
        return returnNode;
    }

    /**
     * Add one node to cluster.
     *
     * @param group
     * @param host
     * @param port
     * @return
     */
    public NodeBean acceptNode(String group, String host, int port) {
        String name = "Test name" + port;//TODO: auto generate node name
        NodeBean addedNode = acceptNode(group, name, host, port);
        LOGGER.info(String.format("Node %s:%d is added succesfully", host, port));
        return addedNode;
    }

    /**
     * Add one node to cluster.
     *
     * @param group
     * @param name
     * @param host
     * @param port
     * @return
     */
    private NodeBean acceptNode(String group, String name, String host, int port) {
        NodeBean node = null;
        nodeLocker.lock();
        try {
            String hashId = StringUtil.getHashAddress(host, port);
            node = new NodeBean(hashId, name, host, port);
            node.setGroup(group);
            ConcurrentMap<String, NodeBean> groupMap = groupedNodeMap.get(group);
            if (null == groupMap) {
                groupMap = new ConcurrentHashMap();
                ConcurrentMap<String, NodeBean> putIfAbsent = groupedNodeMap.putIfAbsent(group, groupMap);
                groupMap = putIfAbsent == null ? groupMap : putIfAbsent;
            }
            AbstractGroupManager manager = managers.get(group);
            if (null != manager) {
                manager.addNodeBean(node);
            }
            groupMap.putIfAbsent(hashId, node);
            nodeGroup.putIfAbsent(hashId, node);
            serverNode.addNode(hashId, node);
            //
            newNode.set(node);
            newNodeCondition.signalAll();
        } catch (Exception ex) {
            LOGGER.error("Could not add new node to cluster, error: ", ex);
        } finally {
            nodeLocker.unlock();
            return node;
        }
    }

    /**
     * Edit node information, such as name and group. If the group is changed,
     * node will be moved to group and their manager, if it exists.
     *
     * @param group
     * @param name
     * @param host
     * @param port
     * @return
     */
    public boolean editNode(String group, String name, String host, int port) {
        String hashId = StringUtil.getHashAddress(host, port);
        boolean res = false;
        try {
            NodeBean editingNode = nodeGroup.get(hashId);
            synchronized (editingNode) {
                String currName = editingNode.getName();
                editingNode.setName(name);
                String currGroup = editingNode.getGroup();
                if (!currGroup.equalsIgnoreCase(group)) {
                    AbstractGroupManager currManager = managers.get(currGroup);
                    LOGGER.info("Start Editing node info with group[" + currGroup + "], name: [" + currName
                            + "] to new group: [" + group + "], new name: [" + name + "]");
                    //Start to remove editing group from currrent group
                    groupedNodeMap.get(currGroup).remove(hashId);
                    if (null != currManager) {
                        currManager.removeNodeBean(editingNode);
                    }
                    editingNode.setGroup(group);

                    //Start to add editing group to new group
                    ConcurrentMap<String, NodeBean> groupMap = groupedNodeMap.get(group);
                    if (null == groupMap) {
                        groupMap = new ConcurrentHashMap();
                        ConcurrentMap<String, NodeBean> putIfAbsent = groupedNodeMap.putIfAbsent(group, groupMap);
                        groupMap = putIfAbsent == null ? groupMap : putIfAbsent;
                    }
                    groupMap.put(hashId, editingNode);
                    //Check if this node group have manager, if it exists, this node will be added to their manager.
                    AbstractGroupManager manager = managers.get(group);
                    if (null != manager) {
                        manager.addNodeBean(editingNode);
                    }
                    LOGGER.info("Complete edit node info with group[" + currGroup + "], name: [" + currName
                            + "] to new group: [" + group + "], new name: [" + name + "]");
                }
            }
            res = true;
        } catch (Exception ex) {
            LOGGER.error("Could not editing nodeId: " + hashId + ", error: ", ex);
        }
        return res;
    }

    /**
     * Remove one node from cluster
     *
     * @param group
     * @param host
     * @param port
     */
    public void removeNode(String group, String host, int port) {
        String hashId = StringUtil.getHashAddress(host, port);
        ConcurrentMap<String, NodeBean> nodeMap = groupedNodeMap.get(group);
        if (null != nodeMap) {
            nodeMap.remove(hashId);
        }
        AbstractGroupManager manager = managers.get(group);
        if (null != manager) {
            manager.removeNodeBean(hashId);
        }
        nodeGroup.remove(hashId);
        serverNode.removeNode(hashId);
    }

    /**
     * Send message to all clients in a group
     *
     * @param channel
     * @param group
     * @param message
     * @throws java.lang.InterruptedException
     * @throws java.util.concurrent.ExecutionException
     */
    public void sendMessageToGroupClient(String channel, String group, String message) throws InterruptedException, ExecutionException {
        ConcurrentMap<String, NodeBean> groupNode = groupedNodeMap.get(group);
        if (null == groupNode || groupNode.isEmpty()) {
            return;
        }
        for (Map.Entry<String, NodeBean> entry : groupNode.entrySet()) {
            if (entry.getValue().getStatus() != NodeBean.DIED) {
                serverNode.sendMessageToSingleClient(channel, entry.getKey(), message);
            }
        }
    }

    /**
     * Return the specified node's status
     *
     * @param nodeId
     * @return
     */
    public String checkNodeStatus(String nodeId) {
        return serverNode.checkNodeStatus(nodeId);
    }
}

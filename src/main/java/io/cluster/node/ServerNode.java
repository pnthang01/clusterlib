/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package io.cluster.node;

import io.cluster.bean.NodeBean;
import io.cluster.core.IMessageListener;
import io.cluster.listener.ServerHardwareMonitoringListener;
import io.cluster.net.NIOAsyncServer;
import io.cluster.node.manager.NodeManager;
import io.cluster.util.Constants.Channel;
import io.cluster.util.ShutdownHookCleanUp;
import io.cluster.util.StringUtil;
import io.cluster.util.ThreadPoolUtil;
import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 *
 * @author thangpham
 */
public class ServerNode {

    private static final Logger LOGGER = LogManager.getLogger(ServerNode.class.getName());

    private final ConcurrentMap<String, NodeBean> nodeMap;

    private final NIOAsyncServer server;
    private NodeManager nodeManager;
    private static ServerNode _instance = null;
    private ShutdownHookCleanUp shutdownHook = ShutdownHookCleanUp.load();

    private ServerNode() throws IOException {
//        System.setProperty("logFileName", "server-node");
        //
        server = new NIOAsyncServer();
        ThreadPoolUtil.load().addThread(server, 0, -1, TimeUnit.MILLISECONDS);
        shutdownHook.addService(server);
        this.nodeMap = new ConcurrentHashMap();
    }

    /**
     * Run a task will check all nodes are still alive or not per 3 seconds.
     */
    public void _init() {
        //****** Have to read config to read channel 
        //Add default listener to master
        ServerHardwareMonitoringListener listener = new ServerHardwareMonitoringListener();
        server.addListener(Channel.SYSTEM_CHANNEL, listener);
        //Add default manager to master 
        nodeManager = NodeManager.load();
    }

    /**
     * Delegate method to add listener to async server
     *
     * @param channel
     * @param listener
     */
    public void addListener(String channel, IMessageListener listener) {
        server.addListener(channel, listener);
    }

    public synchronized static void initialize() {
        try {
            _instance = new ServerNode();
            _instance._init();
        } catch (Exception ex) {
            LOGGER.error("Cannot not start Server Node ", ex);
            _instance.server.shutdown();
        }
    }

    public synchronized static ServerNode load() {
        if (null == _instance) {
            throw new NullPointerException("Server node isn't initialized yet.");
        }
        return _instance;
    }

    /**
     * Send message to all clients
     *
     * @param channel
     * @param message
     * @throws java.lang.InterruptedException
     * @throws java.util.concurrent.ExecutionException
     */
    public void sendMessageToAllClient(String channel, String message) throws InterruptedException, ExecutionException {
        server.sendMessageToAllClient(channel, message);
    }

    /**
     * Send message to single client base on its id.
     *
     * @param hashId
     * @param channel
     * @param message
     * @return
     * @throws java.lang.InterruptedException
     * @throws java.util.concurrent.ExecutionException
     */
    public boolean sendMessageToSingleClient(String hashId, String channel, String message) throws InterruptedException, ExecutionException {
        if (nodeMap.containsKey(hashId)) {
            server.sendMessageToSingleClient(hashId, channel, message);
            return true;
        } else {
            return false;
        }
    }

    public boolean sendMessageToSingleClient(String host, int port, String channel, String message) throws InterruptedException, ExecutionException {
        String hashId = StringUtil.getHashAddress(host, port);
        return sendMessageToSingleClient(hashId, channel, message);
    }

    /**
     * Check all node statues. For debug only
     *
     * @return
     */
    @Deprecated
    public String checkAllNodeStatus() {
        StringBuilder sb = new StringBuilder();
        int index = 1;
        for (Map.Entry<String, NodeBean> nodeEntry : nodeMap.entrySet()) {
            sb.append("\n").append(index).append(".").append(nodeEntry.getValue().toString());
        }
        //
        return sb.toString();
    }

    /**
     * Return the specified node's status
     *
     * @param hashId
     * @return
     */
    public String checkNodeStatus(String hashId) {
        return nodeMap.get(hashId).toString();
    }

    /**
     * Add one node to cluster
     *
     * @param hashId
     * @param node
     */
    public void addNode(String hashId, NodeBean node) {
        nodeMap.putIfAbsent(hashId, node);
    }

    /**
     * Remove one node from cluster
     *
     * @param hashId
     */
    public void removeNode(String hashId) {
        nodeMap.remove(hashId);
    }

    /**
     * Ping from one node to make the node isn't timeout
     *
     * @param host
     * @param port
     */
    public void ping(String host, int port) {
        String hashId = StringUtil.getHashAddress(host, port);
        NodeBean node = nodeMap.get(hashId);
        if (null != node) {
            node.ping();
        }
    }

    /**
     * Get one node by host and port
     *
     * @param host
     * @param port
     * @return
     */
    public NodeBean getNodeByHostPort(String host, int port) {
        String hashId = StringUtil.getHashAddress(host, port);
        return nodeMap.get(hashId);
    }

    /**
     * Get one node by there id
     *
     * @param id
     * @return
     */
    public NodeBean getNodeById(String id) {
        return nodeMap.get(id);
    }

    /**
     * Get one node by there index, if you're don't truly know the index, use
     * checkNodeStatues() to display all nodes
     *
     * @param index
     * @return
     */
    @Deprecated
    public NodeBean getNodeByIndex(int index) {
        int count = 0;
        for (Map.Entry<String, NodeBean> entry : nodeMap.entrySet()) {
            if (count == index) {
                return entry.getValue();
            }
            ++count;
        }
        return null;
    }
}

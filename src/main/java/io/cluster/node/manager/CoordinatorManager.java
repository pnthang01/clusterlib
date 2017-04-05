/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package io.cluster.node.manager;

import io.cluster.bean.NodeBean;
import io.cluster.core.AbstractGroupManager;
import io.cluster.listener.ServerCoordinatorMessageListener;
import io.cluster.util.Constants;
import io.cluster.util.MethodUtil;
import io.cluster.util.StringUtil;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ExecutionException;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 *
 * @author thangpham
 */
public class CoordinatorManager extends AbstractGroupManager<ServerCoordinatorMessageListener> {

    private static final Logger LOGGER = LogManager.getLogger(CoordinatorManager.class.getName());

    private static CoordinatorManager _instance;

    private void init() {
        serverNode.addListener(Constants.Channel.COORDINATOR_CHANNEL, new ServerCoordinatorMessageListener());
    }

    public static CoordinatorManager load() throws NullPointerException {
        if (null == _instance) {
            LOGGER.info("This CoordinatorManager has not initialized yet. Create new...");
            _instance = new CoordinatorManager();
            _instance.init();
        }
        return _instance;
    }

    public CoordinatorManager() {
        super();
    }

    /**
     * Create new node base on Coordinator node group. This will balance nodes
     * on servers by number of nodes on per server.
     *
     * @return
     * @throws java.lang.InterruptedException
     * @throws java.util.concurrent.ExecutionException
     */
    public boolean createNewNode() throws InterruptedException, ExecutionException {
        NodeBean minUsageNode = null;
        int min = 9999;
        for (Entry<String, NodeBean> entry : nodeGroup.entrySet()) {
            NodeBean value = entry.getValue();
            int usage = StringUtil.safeParseInt(value.getState().get("num_of_nodes"));
            if (usage == 0) {
                minUsageNode = value;
                break;
            }
            if (usage < min) {
                minUsageNode = value;
                min = usage;
            }
        }
        return createNewNode(minUsageNode.getHost(), minUsageNode.getPort());
    }

    /**
     * Create new node base on Coordinator node group. If not Coordinator group
     * does not exist or could not create any new node, result will be false,
     * otherwise true. Initialization parameters are default.
     *
     * @param host
     * @param port
     * @return
     * @throws InterruptedException
     * @throws ExecutionException
     */
    public boolean createNewNode(String host, int port) throws InterruptedException, ExecutionException {
        Map<String, String> sendingMessage = new HashMap();
        sendingMessage.put("action", Constants.Action.START_ACTION);
        return createNewNode(host, port, sendingMessage);
    }

    /**
     * Create new node base on Coordinator node group. If not Coordinator group
     * does not exist or could not create any new node, result will be false,
     * otherwise true.
     *
     * @param host
     * @param port
     * @param sendingMessage
     * @return
     * @throws java.lang.InterruptedException
     * @throws java.util.concurrent.ExecutionException
     */
    public boolean createNewNode(String host, int port, Map<String, String> sendingMessage) throws InterruptedException, ExecutionException {
        return serverNode.sendMessageToSingleClient(host, port, Constants.Channel.COORDINATOR_CHANNEL, MethodUtil.toJson(sendingMessage));
    }

}

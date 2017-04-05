/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package io.cluster.http.controller;

import io.cluster.http.annotation.RequestMapping;
import io.cluster.http.annotation.RequestParam;
import io.cluster.http.core.AbstractController;
import io.cluster.http.core.ResponseModel;
import io.cluster.node.manager.CoordinatorManager;
import io.cluster.node.ServerNode;
import io.cluster.node.manager.NodeManager;
import io.cluster.util.MethodUtil;
import io.cluster.util.StringUtil;
import java.util.Map;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 *
 * @author thangpham
 */
@RequestMapping(uri = "/node")
public class NodeController extends AbstractController {

    private static final Logger LOGGER = LogManager.getLogger(NodeController.class.getName());

    private final String API_KEY = "abc";
    private final ServerNode master = ServerNode.load();
    private final NodeManager nodeManager = NodeManager.load();
    private final CoordinatorManager coordinatorManager = CoordinatorManager.load();

    @RequestMapping(uri = "/all_status")
    public Object getAllStatus() {
        try {
            return master.checkAllNodeStatus();
        } catch (Exception ex) {
            LOGGER.error("Cannot get all node statues ", ex);
            return "Cannot get all node statues, error: " + ex.getMessage();
        }
    }

    @RequestMapping(uri = "/send")
    public Object sendMessageToNode(
            @RequestParam(name = "channel", required = true) String channel,
            @RequestParam(name = "address", required = true) String address,
            @RequestParam(name = "message", required = true) String message) {
        try {
            String[] split = address.split(":");
            String hashAddress = StringUtil.getHashAddress(split[0], Integer.parseInt(split[1]));
            master.sendMessageToSingleClient(channel, hashAddress, message);
            return new ResponseModel(1, "success", null);
        } catch (Exception ex) {
            LOGGER.error("Cannot send message to a node ", ex);
            return new ResponseModel(-1, "failed", ex.getMessage());
        }
    }

    @RequestMapping(uri = "/send_all")
    public Object sendMessageToGroup(
            @RequestParam(name = "channel", required = true) String channel,
            @RequestParam(name = "group", required = true) String group,
            @RequestParam(name = "message", required = true) String message) {
        try {
            nodeManager.sendMessageToGroupClient(channel, group, message);
            return new ResponseModel(1, "success", null);
        } catch (Exception ex) {
            LOGGER.error("Cannot send message to group [" + group + "], at channel [" + channel + "] with message: " + message, ex);
            return new ResponseModel(-1, "failed", ex.getMessage());
        }
    }

    @RequestMapping(uri = "/create")
    public Object createNewNode(
            @RequestParam(name = "host", required = false) String host,
            @RequestParam(name = "port", required = false) int port,
            @RequestParam(name = "param", required = false) String param) {
        try {
            boolean res = Boolean.TRUE;
            if ((StringUtil.isNullOrEmpty(host) || port == 0) && StringUtil.isNullOrEmpty(param)) {
                res = coordinatorManager.createNewNode();
            } else if (!StringUtil.isNullOrEmpty(host) && port > 0) {
                res = coordinatorManager.createNewNode(host, port);
            } else if (!StringUtil.isNullOrEmpty(host) && port > 0 && !StringUtil.isNullOrEmpty(param)) {
                Map<String, String> sendingMessage = MethodUtil.fromJsonToMap(param);
                res = coordinatorManager.createNewNode(host, port, sendingMessage);
            }
            if (res) {
                return new ResponseModel(200, "success", null);
            } else {
                return new ResponseModel(-1, "failed", "Cannot create new node.");
            }
        } catch (Exception ex) {
            LOGGER.error("Cannot create message to coordinator ", ex);
            return new ResponseModel(-1, "failed", ex.getMessage());
        }
    }

    @RequestMapping(uri = "/status")
    public Object checkNodeStatus(
            @RequestParam(name = "node_id", required = false) String nodeId) {
        try {
            return new ResponseModel(200, "success", " nodeManager.checkNodeStatus(nodeId);");
        } catch (Exception ex) {
            LOGGER.error("Cannot create message to coordinator ", ex);
            return new ResponseModel(-1, "failed", ex.getMessage());
        }
    }
}

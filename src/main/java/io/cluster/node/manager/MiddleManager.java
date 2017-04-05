/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package io.cluster.node.manager;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 *
 * @author thangpham
 */
public class MiddleManager {

    private static final Logger LOGGER = LogManager.getLogger(NodeManager.class.getName());

    private static MiddleManager _instance;

    private void init() {
//        masterNode.addListener(Constants.Channel.NODE_CHANNEL, new ServerNodeMessageListener());
//        //TODO: will change to configuration
//        managers.put(Constants.Group.TASK_GROUP, TaskManager.load());
//        managers.put(Constants.Group.COORDINATOR_GROUP, CoordinatorManager.load());
    }

    public static synchronized MiddleManager load() throws NullPointerException {
        if (null == _instance) {
            LOGGER.info("This MiddleManager has not initialized yet. Create new...");
            _instance = new MiddleManager();
            _instance.init();
        }
        return _instance;
    }

}

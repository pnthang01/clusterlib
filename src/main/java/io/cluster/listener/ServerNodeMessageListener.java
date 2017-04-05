/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package io.cluster.listener;

import io.cluster.bean.NodeBean;
import io.cluster.bean.RequestNetBean;
import io.cluster.core.IMessageListener;
import io.cluster.node.manager.NodeManager;
import io.cluster.util.Constants;
import io.cluster.util.MethodUtil;
import io.cluster.util.StringUtil;
import java.util.Map;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 *
 * @author thangpham
 */
public class ServerNodeMessageListener extends IMessageListener<RequestNetBean> {

    private static final Logger LOGGER = LogManager.getLogger(ServerNodeMessageListener.class.getName());
    private final NodeManager nodeManager = NodeManager.load();
    
    @Override
    public void onChannel(RequestNetBean bean) {
        String address = bean.getAddress().toString();
        if (address.startsWith("/")) {
            address = address.replace("/", "");
        }
        String[] tmp = address.split(":");
        NodeBean addedNode = nodeManager.acceptNode(Constants.Group.NONE_GROUP, tmp[0], Integer.valueOf(tmp[1]));
    }

    @Override
    public void onMessage(RequestNetBean bean) {
        try {
            String messageStr = bean.getMessageAsString();
            if (null == messageStr) {
                System.err.println("Cannot not process request with null message");
            }
            Map<String, String> message = StringUtil.fromJsonToMap(messageStr);
            String action = message.getOrDefault("action", "");
            switch (action) {
                case Constants.Action.EDIT_ACTION:
                    nodeManager.editNode(message.get("group"), message.get("name"), bean.getHost(), bean.getPort());
                    break;
                case Constants.Action.REPORT_ACTION:
                    break;
                default:
                    LOGGER.error(String.format("Cannot perform request with action: %s", action));
            }
        } catch (Exception ex) {
            LOGGER.error("Receive wrong message: " + MethodUtil.toJson(bean), ex);
        }
    }

}

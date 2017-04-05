/* To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package io.cluster.listener;

import io.cluster.bean.NodeBean;
import io.cluster.bean.RequestNetBean;
import io.cluster.core.IMessageListener;
import io.cluster.node.ServerNode;
import io.cluster.util.Constants.Action;
import io.cluster.util.StringUtil;
import java.util.Map;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 *
 * @author thangpham
 */
public class ServerHardwareMonitoringListener extends IMessageListener<RequestNetBean> {
    
    private static final Logger LOGGER = LogManager.getLogger(ServerNode.class.getName());
    
    private ServerNode masterNode = ServerNode.load();
    
    @Override
    public void onMessage(RequestNetBean request) {
        try {
            RequestNetBean requestBean = (RequestNetBean) request;
            if (request.getMessage().length == 1) {
                masterNode.ping(requestBean.getHost(), requestBean.getPort());
            } else {
                String messageStr = request.getMessageAsString();
                if (null == messageStr) {
                    System.err.println("Cannot not process request with null message");
                }
                NodeBean node = masterNode.getNodeByHostPort(request.getHost(), request.getPort());
                Map<String, String> message = StringUtil.fromJsonToMap(messageStr);
                String action = message.getOrDefault("action", "");
                switch (action) {
                    case Action.REPORT_ACTION:
                        node.setState(message);
                        break;
                    default:
                        LOGGER.error(String.format("Cannot perform request with message: %s", messageStr));
                }
            }
        } catch (Exception ex) {
            LOGGER.error("Strange error occured with error message: ", ex);
        }
    }
    
    @Override
    public void onChannel(RequestNetBean request) {
    }
    
}

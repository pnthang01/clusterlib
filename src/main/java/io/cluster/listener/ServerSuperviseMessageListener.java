/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package io.cluster.listener;

import io.cluster.bean.NodeBean;
import io.cluster.bean.RequestNetBean;
import io.cluster.core.IMessageListener;
import io.cluster.node.ServerNode;
import io.cluster.node.manager.SuperviseManager;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 *
 * @author thangpham
 */
public class ServerSuperviseMessageListener extends IMessageListener<RequestNetBean> {

    private static final Logger LOGGER = LogManager.getLogger(ServerSuperviseMessageListener.class.getName());
    private ServerNode masterNode = ServerNode.load();
    private SuperviseManager superviseManager = SuperviseManager.load();
    
    @Override
    public void onChannel(RequestNetBean bean) {
//        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void onMessage(RequestNetBean requestBean) {
        try {
            String messageStr = requestBean.getMessageAsString();
            if (null == messageStr) {
                System.err.println("Cannot not process request with null message");
            }
            NodeBean node = masterNode.getNodeByHostPort(requestBean.getHost(), requestBean.getPort());
            superviseManager.receiveMessageFromListener(node.getHost(), node.getPort(), messageStr);
        } catch (Exception ex) {
            LOGGER.error("Strange error occured with error message: ", ex);
        }
    }

}

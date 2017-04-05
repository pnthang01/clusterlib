/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package io.cluster.listener;

import io.cluster.bean.RequestNetBean;
import io.cluster.core.IMessageListener;
import io.cluster.node.manager.TaskManager;

/**
 *
 * @author thangpham
 */
public class ServerTaskMessageListener extends IMessageListener<RequestNetBean> {

    private final TaskManager taskManager = TaskManager.load();

    @Override
    public void onChannel(RequestNetBean bean) {
    }

    @Override
    public void onMessage(RequestNetBean bean) {
        String messageStr = bean.getMessageAsString();
        taskManager.receiveMessageFromListener(bean.getHost(), bean.getPort(), messageStr);
    }

}

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package io.cluster.node.manager;

import io.cluster.model.SuperviseModel;
import io.cluster.core.AbstractGroupManager;
import io.cluster.dao.derby.SuperviseDAO;
import io.cluster.listener.ServerSuperviseMessageListener;
import io.cluster.util.Constants;
import io.cluster.util.MethodUtil;
import io.cluster.util.StringUtil;
import io.cluster.util.ThreadPoolUtil;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.TimeUnit;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 *
 * @author thangpham
 */
public class SuperviseManager extends AbstractGroupManager<ServerSuperviseMessageListener> {

    private static final Logger LOGGER = LogManager.getLogger(SuperviseManager.class.getName());

    private static SuperviseManager _instance;
    private NodeManager nodeManager = null;
    private SuperviseDAO superviseDAO = null;
    private ConcurrentMap<String, SuperviseModel> beanMap = new ConcurrentHashMap();

    public static SuperviseManager load() throws NullPointerException {
        if (null == _instance) {
            LOGGER.info("This NodeManager has not initialized yet. Create new...");
            _instance = new SuperviseManager();
            _instance.init();
        }
        return _instance;
    }

    public SuperviseManager() {
    }

    private void init() {
        serverNode.addListener(Constants.Channel.SUPERVISE_CHANNEL, new ServerSuperviseMessageListener());
        nodeManager = NodeManager.load();
        superviseDAO = SuperviseDAO.load();
        superviseDAO.loadAllSuperviseBean().stream().forEach((model) -> {
            beanMap.put(model.getProcessId(), model);
        });
        ThreadPoolUtil threadpool = ThreadPoolUtil.load();
        threadpool.addThread(() -> {
            beanMap.values().stream()
                    .filter((model) -> (model.getTimeout() > 0 && model.getLastUpdated() + model.getTimeout() < System.currentTimeMillis())).forEach((model) -> {

                LOGGER.error("This supervise bean with Id " + model.getProcessId() + " is silence");
                model.setStatus(Constants.Status.SILENCE_STATUS);
                notifyAllNode(model);

            });
        }, 5, 5, TimeUnit.SECONDS);
    }

    private void notifyAllNode(SuperviseModel data) {
        try {
            Map<String, String> map = new HashMap();
            map.put("action", Constants.Action.NOTIFY_ACTION);
            map.put("data", MethodUtil.toJson(data));
            serverNode.sendMessageToAllClient(Constants.Channel.SUPERVISE_CHANNEL, MethodUtil.toJson(map));
        } catch (Exception ex) {
            LOGGER.error("Could not send message to all client on channel: " + Constants.Channel.SUPERVISE_CHANNEL, ex);
        }
    }

    public void receiveMessageFromListener(String callerHost, int callerPort, String messageStr) {
        try {
            if (null == messageStr) {
                serverNode.sendMessageToSingleClient(callerHost, callerPort, Constants.Channel.SUPERVISE_CHANNEL, "Does not received any report.");
                return;
            }
            Map<String, String> message = StringUtil.fromJsonToMap(messageStr);
            String action = message.get("action");
            String address = StringUtil.getHashAddress(callerHost, callerPort);
            String processName = message.get("processName");
            String processId = StringUtil.isNullOrEmpty(message.get("processId")) ? address : message.get("processId");
            switch (action) {
                case Constants.Action.REPORT_ACTION:
                    String status = message.get("status");
                    updateSuperviseProcessStatus(processId, status);
                    break;
                case Constants.Action.REGISTRY_ACTION:
                    long timeout = StringUtil.safeParseLong(message.get("timeout"));
                    registrySuperviseProcess(processId, processName, timeout);
                    break;
                case Constants.Action.STOP_ACTION:
                    stopSuperviseProcess(processId);
                    break;
                default:
                    String returnMess = String.format("Cannot perform request with action:", messageStr);
                    serverNode.sendMessageToSingleClient(callerHost, callerPort, Constants.Channel.SUPERVISE_CHANNEL, returnMess);
                    LOGGER.warn("Could not process action for supervise bean's Id " + processId);
                    break;
            }
        } catch (Exception ex) {
            LOGGER.error("Error when receive message from client: " + callerHost + ":" + callerPort, ex);
            try {
                serverNode.sendMessageToSingleClient(callerHost, callerPort, Constants.Channel.SUPERVISE_CHANNEL,
                        "Error when receive message from client: " + ex.getMessage());
            } catch (Exception ex1) {
            }
        }
    }

    public void stopSuperviseProcess(String processId) {
        SuperviseModel bean = beanMap.remove(processId);
        if (null == bean) {
            throw new NullPointerException("This supervise bean with Id " + processId + " does not exist.");
        }
        bean.setStatus(Constants.Status.STOP_STATUS);
        notifyAllNode(bean);
        superviseDAO.deleteSuperviseBean(bean.getProcessId());
    }

    public void registrySuperviseProcess(String processId, String processName, long timeOut) {
        SuperviseModel bean = beanMap.get(processId);
//        if (timeOut < 1) {
//            throw new IllegalArgumentException("Timeout for this process name must greater than 0");
//        }
        if (null != bean) {
            LOGGER.warn("This supervise bean with Id " + processId + " already exist. Will overwrite.");
            bean.setProcessId(processId).setProcessName(processName).setTimeout(timeOut).setStatus(Constants.Status.RETRYING_STATUS);
            superviseDAO.updateSuperviseBean(bean);
        } else {
            LOGGER.info("This supervise bean with Id " + processId + " will be added to monitor.");
            bean = new SuperviseModel();
            bean.setProcessId(processId).setProcessName(processName).setTimeout(timeOut).setStatus(Constants.Status.START_STATUS);
            superviseDAO.insertSuperviseBean(bean);
        }

        beanMap.put(processId, bean);
    }

    public void updateSuperviseProcessStatus(String processId, String status) {
        SuperviseModel bean = beanMap.get(processId);
        if (null == bean) {
            throw new NullPointerException("The supervise bean with Id " + processId + " does not exist.");
        }
        bean.setStatus(status);
        superviseDAO.updateSuperviseBean(bean);
    }
}

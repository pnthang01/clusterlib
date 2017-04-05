/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package io.cluster.node.manager;

import io.cluster.bean.NodeBean;
import io.cluster.model.TaskModel;
import io.cluster.core.AbstractGroupManager;
import io.cluster.listener.ServerTaskMessageListener;
import io.cluster.util.Constants;
import io.cluster.util.MethodUtil;
import io.cluster.util.StringUtil;
import io.cluster.util.ThreadPoolUtil;
import java.util.Map;
import java.util.Queue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 *
 * @author thangpham
 */
public class TaskManager extends AbstractGroupManager<ServerTaskMessageListener> {

    private static final Logger LOGGER = LogManager.getLogger(TaskManager.class.getName());

    private static TaskManager _instance;

    private CoordinatorManager coorManager;
    private NodeManager nodeManager;
    private final Queue<TaskModel> taskQueue;
    private final ConcurrentMap<String, TaskModel> taskMap;

    private void init() {
        serverNode.addListener(Constants.Channel.TASK_CHANNEL, new ServerTaskMessageListener());
        nodeManager = NodeManager.load();
        coorManager = CoordinatorManager.load();
        run();
    }

    public static TaskManager load() throws NullPointerException {
        if (null == _instance) {
            LOGGER.info("This TaskManager has not initialized yet. Create new...");
            _instance = new TaskManager();
            _instance.init();
        }
        return _instance;
    }

    public TaskManager() {
        taskMap = new ConcurrentHashMap();
        taskQueue = new ConcurrentLinkedQueue<>();
    }

    public void receiveMessageFromListener(String callerHost, int callerPort, String messageStr) {
        try {
            if (null == messageStr) {
                serverNode.sendMessageToSingleClient(callerHost, callerPort, Constants.Channel.TASK_CHANNEL, "Does not have task definition.");
                return;
            }
            Map<String, String> message = StringUtil.fromJsonToMap(messageStr);
            String action = message.getOrDefault("action", "");
            switch (action) {
                case Constants.Action.REPORT_ACTION:
                    String taskId = message.get("taskId");
                    String status = message.get("status");
                    String messageReport = message.get("message");
                    TaskModel newValueTask = new TaskModel()
                            .setTaskId(taskId)
                            .setStatus(status)
                            .setMessageReport(messageReport);
                    this.updateTaskValue(taskId, newValueTask);
                    break;
                case Constants.Action.START_ACTION:
                    String address = message.get("address");
                    String[] split = address.split(":");
                    if (split.length != 2) {
                        serverNode.sendMessageToSingleClient(callerHost, callerPort, Constants.Channel.TASK_CHANNEL,
                                "You must specify either on a node or on a coordinator");
                        return;
                    }
                    Map<String, String> task = MethodUtil.fromJsonToMap(message.get("task"));
                    boolean newProcess = StringUtil.safeParseBoolean(message.get("new_process"), false);
                    int parallel = StringUtil.safeParseInt(message.get("parallel"), 1);
                    int delay = StringUtil.safeParseInt(message.get("delay"), 3000);
                    String env = message.get("environment");
                    int period = StringUtil.safeParseInt(message.get("period"), 0);
                    TaskModel taskBean = new TaskModel()
                            .setHost(split[0])
                            .setPort(StringUtil.safeParseInt(split[1]))
                            .setOnNewProcess(newProcess)
                            .setDelay(delay)
                            .setPeriod(period)
                            .setParallelProcess(parallel)
                            .setInstructions(task)
                            .setEnv(env);
                    this.addTask(taskBean);
                    break;
                case Constants.Action.EDIT_ACTION:
                    break;
                default:
                    System.err.println(String.format("Cannot perform request with action:", messageStr));
            }
        } catch (Exception ex) {
            try {
                serverNode.sendMessageToSingleClient(callerHost, callerPort, Constants.Channel.TASK_CHANNEL, "Error when receive message from client");
            } catch (Exception ex1) {
            } 
        }
    }

    public int updateTaskValue(String taskId, TaskModel otherValue) {
        int res = 0;
        try {
            TaskModel currValue = taskMap.get(taskId);
            res = currValue.updateValue(otherValue);
        } catch (Exception ex) {
            LOGGER.error("Could not add update task value with Id: " + taskId + ", error", ex);
        }
        return res;
    }

    public boolean assignTaskToClient(String host, int port, Map<String, String> sendingMessage) throws InterruptedException, ExecutionException {
        serverNode.sendMessageToSingleClient(host, port, Constants.Channel.TASK_CHANNEL, MethodUtil.toJson(sendingMessage));
        return true;
    }

    /**
     * Add task to queue
     *
     * @param task
     * @return
     */
    public String addTask(TaskModel task) {
        String taskId = null;
        try {
            task.setSubmitedTimed(System.currentTimeMillis());
            taskId = StringUtil.buildTaskId(task);
            task.setTaskId(taskId);
            task.setStatus(Constants.Status.WAIT_STATUS);
            taskMap.put(taskId, task);
            taskQueue.add(task);
        } catch (Exception ex) {
            LOGGER.error("Could not add new task, error", ex);
        }
        return taskId;

    }

    private void run() {
        ThreadPoolUtil threadpool = ThreadPoolUtil.load();
        threadpool.addThread(() -> {
            TaskModel task = null;
            try {
                int size = taskQueue.size();
                while (size > 0) {
                    task = taskQueue.poll();
                    task.setStatus(Constants.Status.RUNNING_STATUS);
                    --size;
                    if (System.currentTimeMillis() > task.getSubmitedTimed() + task.getDelay()) {
                        String host;
                        int port;
                        if (task.isOnNewProcess()) {
                            boolean res = false;
                            if (task.getHost() != null && task.getPort() != 0) {
                                if (StringUtil.isNullOrEmpty(task.getEnv())) {
                                    res = coorManager.createNewNode(task.getHost(), task.getPort());
                                } else {
                                    Map<String, String> env = MethodUtil.fromJsonToMap(task.getEnv());
                                    res = coorManager.createNewNode(task.getHost(), task.getPort(), env);
                                }
                            } else {
                                res = coorManager.createNewNode();
                            }
                            if (!res) {
                                LOGGER.error("Could not create new node, consider wrong coordinator or no coordinator.");
                                break;
                            }
                            NodeBean newNode = nodeManager.waitingForNewNode();
                            host = newNode.getHost();
                            port = newNode.getPort();
                        } else {
                            host = task.getHost();
                            port = task.getPort();
                        }
                        assignTaskToClient(host, port, task.getInstructions());
                        LOGGER.info("Assign task to node: " + host + ":" + port + " with detail: " + MethodUtil.toJson(task));
                    } else {
                        taskQueue.add(task);
                    }
                }
            } catch (Exception ex) {
                LOGGER.error("Could not assign task: " + MethodUtil.toJson(task), ex);
            }
        }, 1, 1, TimeUnit.SECONDS);
    }
}

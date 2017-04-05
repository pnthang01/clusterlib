/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package io.cluster.http.controller;

import io.cluster.model.TaskModel;
import io.cluster.http.annotation.RequestMapping;
import io.cluster.http.annotation.RequestParam;
import io.cluster.http.core.AbstractController;
import io.cluster.http.core.ResponseModel;
import io.cluster.node.manager.TaskManager;
import io.cluster.util.StringUtil;
import java.util.Map;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 *
 * @author thangpham
 */
@RequestMapping(uri = "/task")
public class TaskController extends AbstractController {

    private static final Logger LOGGER = LogManager.getLogger(NodeController.class.getName());
    private final TaskManager taskControl = TaskManager.load();

    @RequestMapping(uri = "/status")
    public Object getTaskStatus(
            @RequestParam(name = "taskId", required = true) String taskId) {
        return null;
    }

    @RequestMapping(uri = "/assign")
    public Object assignTaskToNode(
            @RequestParam(name = "address", required = true) String address,
            @RequestParam(name = "task", required = true) Map<String, String> task,
            @RequestParam(name = "new_process", required = true) boolean newProcess,
            @RequestParam(name = "parallel", required = false, defaultValue = "1") int parallelProcess,
            @RequestParam(name = "delay", required = false, defaultValue = "3000") int delay,
            @RequestParam(name = "environment", required = false, defaultValue = "") String env,
            @RequestParam(name = "period", required = true, defaultValue = "0") int period) {
        ResponseModel response = null;
        try {
            if (task == null || task.isEmpty()) {
                LOGGER.error("No task has definied. Assign task failed.");
                response = new ResponseModel(-1, "failed", "No task has definied. Assign task failed.");
            } else {
                String[] split = address.split(":");
                if (split.length != 2) {
                    response = new ResponseModel(-1, "failed", "You must specify either on a node or on a coordinator");
                } else {
                    TaskModel taskBean = new TaskModel()
                            .setHost(split[0])
                            .setPort(StringUtil.safeParseInt(split[1]))
                            .setOnNewProcess(newProcess)
                            .setDelay(delay)
                            .setPeriod(period)
                            .setParallelProcess(parallelProcess)
                            .setInstructions(task)
                            .setEnv(env);
                    taskControl.addTask(taskBean);
                    response = new ResponseModel(1, "success", null);
                }
            }
        } catch (Exception ex) {
            LOGGER.error("Cannot send task to a node ", ex);
            response = new ResponseModel(-1, "failed", ex.getMessage());
        }
        return response;
    }
}

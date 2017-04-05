/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package demo;

import io.cluster.ClusterLibInitializer;
import io.cluster.bean.NodeBean;
import io.cluster.model.TaskModel;
import io.cluster.node.ServerNode;
import io.cluster.node.manager.SuperviseManager;
import io.cluster.util.Constants;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;

/**
 *
 * @author thangpham
 */
public class TestServer {

    public static void main(String[] args) throws IOException {
//        System.setProperty("log4j.configurationFile", "configs/server-config/log4j2.xml");
        ClusterLibInitializer init = new ClusterLibInitializer("./configs/server-config/");
        int choice = 0;
        Scanner sc = new Scanner(System.in);
        ServerNode server = ServerNode.load();
        SuperviseManager.load();
        do {
            try {
                System.out.println("1. Send message");
                System.out.println("2. Send message to single client");
                System.out.println("3. Monitor clients");
                System.out.println("4. Send tasks");
                choice = sc.nextInt();
                switch (choice) {
                    case 1:
                        server.sendMessageToAllClient("testchannel", "Send message: " + System.currentTimeMillis());
                        break;
                    case 2:
                        System.out.println(server.checkAllNodeStatus());
                        int node = -1;
                        node = sc.nextInt() - 1;
                        String id = server.getNodeByIndex(node).getId();
                        server.sendMessageToSingleClient(id, "testchannel", "Send message");
                    case 3:
                        System.out.println(server.checkAllNodeStatus());
                        break;
                    case 4:
                        System.out.println("Type your node index to assign");
                        String[] assignNodes = sc.next().split(",");
                        Map<String, String> instructions = new HashMap();
                        for (int i = 0; i < assignNodes.length; ++i) {
                            String nodeId = assignNodes[i];
                            NodeBean nodeByIndex = server.getNodeByIndex(Integer.valueOf(nodeId));
                            TaskModel task = new TaskModel();
                            task.setDelay(0);
                            task.setHost(nodeByIndex.getHost());
                            task.setPort(nodeByIndex.getPort());
                            instructions.put("data", (i + 1) + "");
                            instructions.put("loggedTime", System.currentTimeMillis() + "");
                            task.setInstructions(instructions);
//                            taskManager.addTask(task);
                        }
                        break;
                }
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        } while (choice != 0);
    }
}

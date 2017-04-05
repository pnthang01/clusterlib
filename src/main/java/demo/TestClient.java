/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package demo;

import com.google.common.collect.ImmutableMap;
import io.cluster.ClusterLibInitializer;
import io.cluster.bean.INetBean;
import io.cluster.bean.ResponseNetBean;
import io.cluster.core.IMessageListener;
import io.cluster.listener.ClientSuperviseMessageListener;
import io.cluster.listener.ClientTaskMessageListener;
import io.cluster.node.ClientNode;
import io.cluster.util.Constants;
import io.cluster.util.MethodUtil;
import io.cluster.util.StringUtil;
import io.cluster.util.ThreadPoolUtil;
import java.io.IOException;
import java.net.SocketAddress;
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

/**
 *
 * @author thangpham
 */
public class TestClient {

    public static void main(String[] args) throws IOException, InterruptedException, ExecutionException {
        ClusterLibInitializer init = new ClusterLibInitializer("./configs/client-config/");
        ClientNode client = ClientNode.load();
        client.addListener("testchannel", new ClientTestChannel());
        client.addListener(Constants.Channel.TASK_CHANNEL, new ClientTaskMessageListener() {
            @Override
            public void onChannel(ResponseNetBean bean) {
            }

            @Override
            public void onMessage(ResponseNetBean bean) {
                String messageStr = bean.getMessageAsString();
                Map<String, String> message = StringUtil.fromJsonToMap(messageStr);
                System.out.println("Received at: " + System.currentTimeMillis() + ", data" + message.get("data")
                        + " submitedTime: " + message.get("loggedTime"));
            }
        });
        client.addListener(Constants.Channel.SUPERVISE_CHANNEL, new ClientSuperviseMessageListener());
        ImmutableMap<String, String> map = ImmutableMap.<String, String>builder()
                .put("action", Constants.Action.REGISTRY_ACTION)
                .put("timeout", "5000")
                .put("processName", "Test-process")
                .build();
        client.sendRequest(Constants.Channel.SUPERVISE_CHANNEL, MethodUtil.toJson(map));
        runUpdateStatus();
        int choice = -1;
        Scanner sc = new Scanner(System.in);
        do {
            try {
                System.out.println("1. Send request");
                System.out.println("2. Print remote address");
                choice = sc.nextInt();
                switch (choice) {
                    case 1:
                        client.sendRequest("testchannel", "Send request");
                        break;
                    case 2:
                        SocketAddress remoteAddress = client.getLocalAddress();
                        System.out.println("Address: " + remoteAddress.toString());
                        break;
                }
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        } while (choice != 0);
    }

    private static int totalSend = 5;

    private static void runUpdateStatus() {
        ThreadPoolUtil.load().addThread(new Runnable() {
            @Override
            public void run() {
                try {
                    ClientNode client = ClientNode.load();
                    if (totalSend > 0) {
                        Map<String, String> request = new HashMap();
                        request.put("action", Constants.Action.REPORT_ACTION);
                        request.put("status", Constants.Action.RUN_ACTION);
                        client.sendRequest(Constants.Channel.SUPERVISE_CHANNEL, StringUtil.toJson(request));
                        System.out.println("Send update at " + totalSend);
                    } else if(totalSend == 0) {
                        Map<String, String> request = new HashMap();
                        request.put("action", Constants.Action.STOP_ACTION);
                        client.sendRequest(Constants.Channel.SUPERVISE_CHANNEL, StringUtil.toJson(request));
                        System.out.println("Send stop at " + totalSend);
                    } else {
                        System.out.println("This process is stopped, won't send anymore message");
                    }
                    totalSend--;

                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            }
        }, 5, 5, TimeUnit.SECONDS);
    }

    public static class ClientTestChannel extends IMessageListener {

        @Override
        public void onChannel(INetBean bean) {
        }

        @Override
        public void onMessage(INetBean bean) {
            if (null == bean || !(bean instanceof ResponseNetBean)) {
                return;
            }
            ResponseNetBean response = (ResponseNetBean) bean;
            String message = response.getMessageAsString();
            System.out.println("Receive message from server: " + message + " at: " + System.currentTimeMillis());
        }
    }
}

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package io.cluster.net;

import io.cluster.bean.RequestNetBean;
import io.cluster.config.ApplicationConfig;
import io.cluster.core.IMessageListener;
import io.cluster.core.IService;
import io.cluster.util.Constants.Channel;
import io.cluster.util.StringUtil;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.AsynchronousServerSocketChannel;
import java.nio.channels.AsynchronousSocketChannel;
import java.util.ArrayList;
import java.util.List;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.ExecutionException;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 *
 * @author thangpham
 */
public class NIOAsyncServer implements IService, Runnable {

    private static final Logger LOGGER = LogManager.getLogger(NIOAsyncServer.class.getName());

    private ApplicationConfig appConfig = ApplicationConfig.load();
    private AsynchronousServerSocketChannel asynSvr;
    private final ConcurrentMap<String, List<IMessageListener>> channelListeners;
    private final ConcurrentMap<String, AsyncSocketClient> clientList;
    private int SIZE;
    private boolean isRunning = true;

    public NIOAsyncServer() throws IOException {
        LOGGER.info("The server is about to start...");
        channelListeners = new ConcurrentHashMap<>();
        clientList = new ConcurrentHashMap<>();
        SIZE = appConfig.clusterBufferSize();
        String host = appConfig.clusterServerHost();
        int port = appConfig.clusterServerPort();
        asynSvr = AsynchronousServerSocketChannel.open();
        asynSvr.bind(new InetSocketAddress(host, port));
        LOGGER.info("The server started successfully.");
    }

    public void shutdown() {
        try {
            isRunning = false;
            asynSvr.close();
            LOGGER.info("Server is shutdown...");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void addListener(String channel, IMessageListener listener) {
        List<IMessageListener> listeners = channelListeners.get(channel);
        if (null == listeners) {
            listeners = new ArrayList<>();
            List<IMessageListener> checkList = channelListeners.putIfAbsent(channel, listeners);
            listeners = checkList == null ? listeners : checkList;
        }
        listeners.add(listener);
    }

    public boolean sendMessageToSingleClient(String id, String channel, String message) throws InterruptedException, ExecutionException {
        boolean sendSuccess = Boolean.FALSE;
        for (Entry<String, AsyncSocketClient> entry : clientList.entrySet()) {
            if (entry.getKey().equals(id)) {
                sendSuccess = Boolean.TRUE;
                entry.getValue().sendResponse(channel, message);
                break;
            }
        }
        return sendSuccess;
    }

    public int sendMessageToAllClient(String channel, String message) throws InterruptedException, ExecutionException {
        int numSuccess = 0;
        for (AsyncSocketClient bean : clientList.values()) {
            try {
                bean.sendResponse(channel, message);
                ++numSuccess;
            } catch (Exception ex) {
                try {
                    ex.printStackTrace();
                    System.err.println("Cannot send message to client: " + bean.soc.getRemoteAddress().toString());
                } catch (Exception e) {
                }
            }
        }
        return numSuccess;
    }

    @Override
    public void run() {
        try {
            while (isRunning) {
                try {
                    AsyncSocketClient bean = new AsyncSocketClient(asynSvr.accept().get(), channelListeners);
                    clientList.put(bean.getClientId(), bean);
                    bean.start();
                } catch (Exception ex) {
                    System.err.println("Cannot accept new client, error " + ex.getMessage());
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public class AsyncSocketClient extends Thread {

        private String clientId;
        private final AsynchronousSocketChannel soc;
        private final ConcurrentMap<String, List<IMessageListener>> channelListeners;

        public AsyncSocketClient(AsynchronousSocketChannel soc, ConcurrentMap<String, List<IMessageListener>> channelListeners) throws IOException {
            this.soc = soc;
            this.channelListeners = channelListeners;
            String[] parseAddress = StringUtil.parseAddress(soc.getRemoteAddress());
            this.clientId = StringUtil.getHashAddress(parseAddress[0], Integer.parseInt(parseAddress[1]));
            for (Entry<String, List<IMessageListener>> entry : channelListeners.entrySet()) {
                for (IMessageListener listener : entry.getValue()) {
                    try {
                        listener.onChannel(new RequestNetBean(soc.getRemoteAddress(), null));
                    } catch (Exception ex) {
                        ex.printStackTrace();
                    }
                }
            }
        }

        public String getClientId() {
            return clientId;
        }

        public void setClientId(String clientId) {
            this.clientId = clientId;
        }

        @Override
        public void run() {
            try {
                readRequest();
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                try {
                    soc.shutdownInput();
                    soc.shutdownOutput();
                    soc.close();
                } catch (IOException ex) {
                }
            }
        }

        /**
         * Send a message to client by channel
         *
         * @param channel
         * @param response
         */
        public void sendResponse(String channel, String response) {
            if (null == channel || null == response) {
                throw new IllegalArgumentException("Either channel or response message is null.");
            }
            try {
                byte[] finalBytes = new byte[32 + SIZE];
                System.arraycopy(channel.getBytes(), 0, finalBytes, 0, channel.length());
                System.arraycopy(response.getBytes(), 0, finalBytes, 32, response.length());
                soc.write(ByteBuffer.wrap(finalBytes)).get();
            } catch (InterruptedException | ExecutionException ex) {
                System.err.println("Error when send response message to client, error: " + ex.getMessage());
                ex.printStackTrace();
            }
        }

        private void readRequest() {
            ByteBuffer bbuf = ByteBuffer.allocateDirect(SIZE);
            int le = -1;
            byte[] bb = new byte[32 + SIZE];
            while (true) {
                try {
                    le = soc.read(bbuf).get();
                    if (le != -1) {
                        if (le == 1) {
                            List<IMessageListener> listeners = channelListeners.get(Channel.SYSTEM_CHANNEL);
                            for (IMessageListener listener : listeners) {
                                listener._onMessage(new RequestNetBean(soc.getRemoteAddress(), bbuf.get()));
                            }
                        } else {
                            bbuf.flip();
                            bbuf.get(bb, 0, le);
                            String channel = new String(bb, 0, 32).trim();
                            byte[] message = new byte[le - 32];
                            System.arraycopy(bb, 32, message, 0, message.length);
                            if (Channel.ALL_CHANNEL.equals(channel)) {
                                for (List<IMessageListener> listeners : channelListeners.values()) {
                                    for (IMessageListener listener : listeners) {
                                        listener._onMessage(new RequestNetBean(soc.getRemoteAddress(), message));
                                    }
                                }
                            } else {
                                List<IMessageListener> listeners = channelListeners.get(channel);
                                if (null != listeners && !listeners.isEmpty()) {
                                    for (IMessageListener listener : listeners) {
                                        listener._onMessage(new RequestNetBean(soc.getRemoteAddress(), message));
                                    }
                                }
                            }
                            bbuf.clear();
                        }
                    }
                } catch (IOException ex) {
                    System.err.println(String.format("Client %s exited or forced to quit, stop listenning.", this.clientId));
                    break;
                } catch (Exception ex) {
                    ex.printStackTrace();
                } finally {
                    bbuf.clear();
                }
            }
        }
    }
}

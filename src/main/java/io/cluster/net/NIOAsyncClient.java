/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package io.cluster.net;

import io.cluster.ClusterLibInitializer;
import io.cluster.bean.ResponseNetBean;
import io.cluster.config.ApplicationConfig;
import io.cluster.core.IMessageListener;
import io.cluster.util.StringUtil;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.AsynchronousSocketChannel;
import java.util.List;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 *
 * @author thangpham
 */
public class NIOAsyncClient extends Thread {

    static final Logger LOGGER = LogManager.getLogger(NIOAsyncClient.class.getName());
    
    private ApplicationConfig appConfig = ApplicationConfig.load();
    private ConcurrentMap<String, List<IMessageListener>> channelListeners;
    private AsynchronousSocketChannel soc;
    int BUFFER_SIZE, pid = -1;
    long PING_PERIOD;
    private Lock selfCloseLock;
    private Condition selfLockCondition;
    private boolean isSelfClosed = Boolean.FALSE;
    private AtomicInteger retries = new AtomicInteger();

    public NIOAsyncClient(ConcurrentMap<String, List<IMessageListener>> channelListeners) {
        try {
            this.channelListeners = channelListeners;
            selfCloseLock = new ReentrantLock();
            selfLockCondition = selfCloseLock.newCondition();
            BUFFER_SIZE = appConfig.clusterBufferSize();
            String serverHost = appConfig.clusterServerHost();
            int serverPort = appConfig.clusterServerPort();
            PING_PERIOD = appConfig.clusterClientPingPeriod();
            soc = AsynchronousSocketChannel.open();
            String clientHost = appConfig.clusterClientHost();
            int clientPort = appConfig.clusterClientPort();
            if(!StringUtil.isNullOrEmpty(clientHost) && clientPort != -1) {
                LOGGER.info("This client will bind to host: " + clientHost + ", port: " + clientPort);
                soc.bind(new InetSocketAddress(clientHost, clientPort));
            }
            LOGGER.info(String.format("Connect to server at %s:%d", serverHost, serverPort));
            soc.connect(new InetSocketAddress(serverHost, serverPort)).get();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    public void run() {
        try {
            pid = 1;
            Ping ping = new Ping(soc);
            ping.start();
            readResponse();
        } catch (Exception ex) {
            ex.printStackTrace();
        } finally {
            close();
        }
    }

    private void selfClose() {
        selfCloseLock.lock();
        try {
            close();
            isSelfClosed = Boolean.TRUE;
            selfLockCondition.signalAll();
        } finally {
            selfCloseLock.unlock();
        }
    }

    public void close() {
        try {
            soc.shutdownInput();
            soc.shutdownOutput();
            soc.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void waitForSelfClose() {
        selfCloseLock.lock();
        try {
            while (!isSelfClosed) {
                selfLockCondition.await();
            }
        } catch (InterruptedException ex) {
            ex.printStackTrace();
        } finally {
            selfCloseLock.unlock();
        }
    }

    public void setChannelListeners(ConcurrentMap<String, List<IMessageListener>> channelListeners) {
        this.channelListeners = channelListeners;
    }

    public SocketAddress getLocalAddress() throws IOException {
        return soc.getLocalAddress();
    }

    public void sendRequest(String channel, String request) {
        try {
            byte[] finalBytes = new byte[32 + BUFFER_SIZE];
            System.arraycopy(channel.getBytes(), 0, finalBytes, 0, channel.length());
            System.arraycopy(request.getBytes(), 0, finalBytes, 32, request.length());
            soc.write(ByteBuffer.wrap(finalBytes)).get();
        } catch (InterruptedException | ExecutionException e) {
            System.err.println("Cannot send request to server, due to lost connection or something.");
            retries.incrementAndGet();
        } catch (Exception ex) {
            System.err.println("Cannot identify the problem, close connection to server.");
            ex.printStackTrace();
            retries.incrementAndGet();
        } finally {
            if (retries.get() > 5) {
                selfClose();
            }
        }
    }

    private void readResponse() throws InterruptedException {
        ByteBuffer bbuf = ByteBuffer.allocateDirect(BUFFER_SIZE);
        int le = -1, retries = 0;
        byte[] bb = new byte[32 + BUFFER_SIZE];
        while (retries < 5) {
            try {
                le = soc.read(bbuf).get();
                if (le != -1) {
                    bbuf.flip();
                    bbuf.get(bb, 0, le);
                    String channel = new String(bb, 0, 32).trim();
                    //
                    List<IMessageListener> listeners = channelListeners.get(channel);
                    if (null != listeners) {
                        byte[] message = new byte[le - 32];
                        System.arraycopy(bb, 32, message, 0, message.length);
                        for (IMessageListener listener : listeners) {
                            listener._onMessage(new ResponseNetBean(soc.getRemoteAddress(), message));
                        }
                    }
                    bbuf.clear();
                }
            } catch (InterruptedException | ExecutionException ex) {
                retries++;
                System.out.println("Cannot connect to server, retry " + retries + " times");
                ex.printStackTrace();
            } catch (Exception ex) {
                retries++;
                System.out.println("Unknown error happends, retry " + retries + " times");
                ex.printStackTrace();
                Thread.sleep(500);
            } finally {
                bbuf.clear();
            }
        }
        selfClose();
    }

    public class Ping extends Thread {

        private AsynchronousSocketChannel soc;
        byte[] ping = new byte[]{1};

        public Ping(AsynchronousSocketChannel soc) {
            this.soc = soc;
        }

        public void run() {
            while (true) {
                try {
                    Thread.sleep(PING_PERIOD);
                    soc.write(ByteBuffer.wrap(ping)).get();
                } catch (Exception ex) {
                }
            }
        }

    }
}

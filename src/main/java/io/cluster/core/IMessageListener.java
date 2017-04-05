/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package io.cluster.core;

import io.cluster.bean.INetBean;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 *
 * @author thangpham
 * @param <T>
 */
public abstract class IMessageListener<T extends INetBean> {

    private final MessageInterceptor interceptor;

    public IMessageListener() {
        interceptor = new MessageInterceptor();
        interceptor.start();
    }

    /**
     * When new channel joins to cluster to give message
     *
     * @param bean
     * @return
     */
    public abstract void onChannel(T bean);

    /**
     * When one channel send message so listener will accept it
     *
     * @param bean
     * @return
     */
    public abstract void onMessage(T bean);

    public void _onMessage(T bean) {
        interceptor.addRequest(bean);
    }

    private class MessageInterceptor extends Thread {

        private final ConcurrentLinkedQueue<T> requestQueue;
        private final Lock lock;
        private final Condition newElement;

        public MessageInterceptor() {
            lock = new ReentrantLock();
            newElement = lock.newCondition();
            requestQueue = new ConcurrentLinkedQueue<>();
        }

        @Override
        public void run() {
            while (true) {
                lock.lock();
                try {
                    while (requestQueue.isEmpty()) {
                        newElement.await();
                    }
                    T request = requestQueue.poll();
                    onMessage(request);
                    //**************************
                } catch (Exception e) {
                    e.printStackTrace();
                } finally {
                    lock.unlock();
                }
            }
        }

        public void addRequest(T request) {
            lock.lock();
            try {
                requestQueue.add(request);
                newElement.signalAll();
            } finally {
                lock.unlock();
            }
        }
    }
}

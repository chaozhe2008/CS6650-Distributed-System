package com.zycao.rmqpool;

import com.rabbitmq.client.Channel;
import java.io.IOException;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.logging.Level;
import java.util.logging.Logger;


/**
 * A simple RabbitMQ channel pool based on a BlockingQueue implementation
 *
 */


public class RMQChannelPool {

    // used to store and distribute channels
    private final BlockingQueue<Channel> pool;
    // fixed size pool
    private int capacity;
    // used to ceate channels
    private RMQChannelFactory factory;
    private String exchangeName;
    private String queueName;


    public RMQChannelPool(int maxSize, RMQChannelFactory factory,
                          String exchangeName,
                          String queueName) {
        this.capacity = maxSize;
        this.pool = new LinkedBlockingQueue<>(capacity);
        this.factory = factory;
        this.exchangeName = exchangeName;
        this.queueName = queueName;
        initializePool();
    }

    private void initializePool() {
        for (int i = 0; i < capacity; i++) {
            try {
                Channel chan = factory.create();
                chan.queueDeclare(queueName, true, false, false, null);
                chan.queueBind(queueName, exchangeName, "");
                pool.put(chan);
            } catch (IOException | InterruptedException ex) {
                Logger.getLogger(RMQChannelPool.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }

    public int getPoolSize(){
        return this.capacity;
    }

    public Channel borrowObject() throws IOException {
        try {
            return pool.take();
        } catch (InterruptedException e) {
            throw new RuntimeException("Error: no channels available" + e.toString());
        }
    }

    public void returnObject(Channel channel) throws Exception {
        if (channel != null) {
            pool.add(channel);
        }
    }

    public void close() {
        // pool.close();
    }
}
package com.zycao.rmqpool;


import com.rabbitmq.client.BuiltinExchangeType;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;

import org.apache.commons.pool2.BasePooledObjectFactory;
import org.apache.commons.pool2.PooledObject;
import org.apache.commons.pool2.impl.DefaultPooledObject;

import java.io.IOException;

public class RMQChannelFactory extends BasePooledObjectFactory<Channel> {

    // Valid RMQ connection
    private final Connection connection;
    // used to count created channels for debugging
    private final String exchangeName;
    private final BuiltinExchangeType exchangeType;
    private int count;

    public RMQChannelFactory(Connection connection, String exchangeName, BuiltinExchangeType exchangeType) {
        this.connection = connection;
        this.exchangeName = exchangeName;
        this.exchangeType = exchangeType;
        count = 0;
    }

    @Override
    synchronized public Channel create() throws IOException {
        count ++;
        Channel chan = connection.createChannel();
        // Uncomment the line below to validate the expected number of channels are being created
        chan.exchangeDeclare(exchangeName, exchangeType, true);
        System.out.println("Channel created: " + count);
        return chan;

    }

    @Override
    public PooledObject<Channel> wrap(Channel channel) {
        return new DefaultPooledObject<>(channel);
    }

    public int getChannelCount() {
        return count;
    }

    // for all other methods, the no-op implementation
    // in BasePooledObjectFactory will suffice
}
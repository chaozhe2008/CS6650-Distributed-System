package com.zycao.rmqFactory;

import com.rabbitmq.client.ConnectionFactory;

public class RMQFactoryConfig {

    private ConnectionFactory factory;
    private static final String HOST = "54.185.2.169";
    private static final int PORT = 5672;
    private static final String USERNAME = "admin";
    private static final String PASSWORD = "password";

    public static final String EXCHANGE_NAME = "skier.direct";
    public static final String QUEUE_NAME = "skierQueue";

    public RMQFactoryConfig() {
        this.factory = new ConnectionFactory();
        factory.setHost(HOST);
        factory.setPort(PORT);
        factory.setUsername(USERNAME);
        factory.setPassword(PASSWORD);
    }

    public ConnectionFactory getFactory() {
        return factory;
    }
}

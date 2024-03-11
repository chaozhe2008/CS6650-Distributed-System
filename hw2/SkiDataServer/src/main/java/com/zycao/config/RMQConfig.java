package com.zycao.config;

import com.rabbitmq.client.BuiltinExchangeType;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import com.zycao.rmqpool.RMQChannelFactory;
import com.zycao.rmqpool.RMQChannelPool;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RMQConfig {

    @Value("${rabbitmq.host}")
    private String host;

    @Value("${rabbitmq.port}")
    private int port;

    @Value("${rabbitmq.username}")
    private String username;

    @Value("${rabbitmq.password}")
    private String password;

    @Value("${rabbitmq.exchangeName}")
    private String exchangeName;

    @Value("${rabbitmq.queueName}")
    private String queueName;

    @Value("${rabbitmq.maxPoolSize}")
    private int poolSize;


    @Bean
    public ConnectionFactory rabbitConnectionFactory() {
        ConnectionFactory connectionFactory = new ConnectionFactory();
        connectionFactory.setHost(host);
        connectionFactory.setPort(port);
        connectionFactory.setUsername(username);
        connectionFactory.setPassword(password);
        return connectionFactory;
    }

    @PostConstruct
    public void postConstruct() {
        System.out.println("Configured RabbitMQ maxPoolSize: " + poolSize);
    }

    @Bean
    public Connection rabbitConnection(ConnectionFactory connectionFactory) throws Exception {
        // Establish and return a connection using the provided ConnectionFactory
        return connectionFactory.newConnection();
    }

    @Bean
    public RMQChannelFactory channelFactory(Connection connection) {
        // Create and return an RMQChannelFactory with the established connection
        return new RMQChannelFactory(connection, exchangeName, BuiltinExchangeType.DIRECT);
    }

    @Bean
    public RMQChannelPool channelPool(RMQChannelFactory factory) {
        // Initialize and return an RMQChannelPool with a desired capacity and the channel factory
        return new RMQChannelPool(poolSize, factory, exchangeName, queueName);
    }


}

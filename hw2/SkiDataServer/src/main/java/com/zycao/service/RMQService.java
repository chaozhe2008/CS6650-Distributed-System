package com.zycao.service;


import com.rabbitmq.client.Channel;
import com.zycao.rmqpool.RMQChannelPool;
import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;

@Service
public class RMQService {
    private final RMQChannelPool channelPool;
    private final ExecutorService executorService;
    private BlockingQueue<String> messageQueue;
    private static final Logger logger = LoggerFactory.getLogger(RMQService.class);

    @Value("${rabbitmq.exchangeName}")
    private String exchangeName;

    @Autowired
    public RMQService(RMQChannelPool channelPool) {
        this.channelPool = channelPool;
        this.executorService = Executors.newFixedThreadPool(channelPool.getPoolSize());
        this.messageQueue = new LinkedBlockingQueue<>();
    }

    @PostConstruct
    public void init() {
        startQueueProcessor();
    }

    private void startQueueProcessor() {
        logger.info("{} threads start publishing messages", channelPool.getPoolSize());
        for (int i = 0; i < channelPool.getPoolSize(); i++) {
            executorService.submit(() -> {
                while (!Thread.currentThread().isInterrupted()) {
                    try {
                        String message = messageQueue.take(); // Block until a message is available
                        publishMessage(exchangeName, "", message);
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                        logger.error("Queue processing thread interrupted", e);
                    } catch (Exception e) {
                        logger.error("Error processing message from queue", e);
                    }
                }
            });
        }
    }

    public void enqueueMessage(String message){
        try {
            messageQueue.put(message);
            logger.info("Current messages waiting to be published: {}", messageQueue.size());
        } catch (InterruptedException e) {
            logger.error("Failed to enqueue message");
        }
    }

    private void publishMessage(String exchangeName, String routingKey, String message) {
        Channel channel = null;
        try {
            channel = channelPool.borrowObject();
            channel.basicPublish(exchangeName, routingKey, null, message.getBytes());
            logger.info("Thread " + Thread.currentThread().getId() + " published to RabbitMQ: " + message);
        } catch (IOException e) {
            logger.error("IOException occurred while publishing message: {}", e.getMessage());
        } catch (Exception e) {
            logger.error("Unexpected exception occurred: {}", e.getMessage());
        } finally {
            if (channel != null) {
                try {
                    channelPool.returnObject(channel);
                } catch (Exception e) {
                    logger.error("Error returning channel to pool: {}", e.getMessage());
                }
            }
        }
    }
}

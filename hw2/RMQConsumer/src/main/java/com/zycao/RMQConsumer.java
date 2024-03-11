package com.zycao;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.rabbitmq.client.*;
import com.zycao.entity.SkiDataPayload;
import com.zycao.rmqFactory.RMQFactoryConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicLong;

import static com.zycao.rmqFactory.RMQFactoryConfig.QUEUE_NAME;

public class RMQConsumer {
    private static final ConcurrentHashMap<Integer, List<SkiDataPayload>> skierDataMap = new ConcurrentHashMap<>();
    private static ExecutorService threadPool;
    private static ConnectionFactory factory;
    private static Connection connection;
    private static final AtomicLong messagesProcessed = new AtomicLong(0);
    private static final Logger logger = LoggerFactory.getLogger(RMQConsumer.class);



    public static void main(String[] argv) throws Exception {
        int threadPoolSize = 8; //Default size
        if (argv.length > 0) {
            try {
                // Parse the first argument as an integer for the thread pool size
                threadPoolSize = Integer.parseInt(argv[0]);
            } catch (NumberFormatException e) {
                logger.error("Invalid thread pool size provided, using default: {}", threadPoolSize);
            }
        }
        logger.info("Consumers Ready, ThreadPool Size: {}", threadPoolSize);

        threadPool = Executors.newFixedThreadPool(threadPoolSize);
        RMQFactoryConfig factoryConfig = new RMQFactoryConfig();
        factory = factoryConfig.getFactory();
        connection = factory.newConnection();

        for (int i = 0; i < threadPoolSize; i++) {
            final int consumerId = i;
            threadPool.submit(() -> {
                try {
                    consumeMessage(consumerId);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            });
        }

        // Add shutdown hook to close the connection properly on shutdown
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            try {
                if (connection != null) {
                    connection.close();
                }
                if (threadPool != null) {
                    threadPool.shutdownNow();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }));
    }

    private static void consumeMessage(int consumerId) throws Exception {
        Channel channel = null;
        try {
            channel = connection.createChannel();
            channel.queueDeclare(QUEUE_NAME, true, false, false, null);
            channel.basicQos(1);

            Consumer consumer = new DefaultConsumer(channel) {
                @Override
                public void handleDelivery(String consumerTag, Envelope envelope, AMQP.BasicProperties properties, byte[] body) throws IOException {
                    String message = new String(body, "UTF-8");
                    logger.info("Consumer {} + on Thread ID: {}", consumerId, Thread.currentThread().getId());
                    processMessage(message);
                }
            };

            String consumerTag = channel.basicConsume(QUEUE_NAME, true, consumer);

        } catch (IOException e) {
            e.printStackTrace();
            if (channel != null) {
                try {
                    channel.close();
                } catch (IOException | TimeoutException ex) {
                    ex.printStackTrace();
                }
            }
        }
    }


    private static void processMessage(String jsonPayload) {
        try {
            ObjectMapper mapper = new ObjectMapper();
            SkiDataPayload payload = mapper.readValue(jsonPayload, SkiDataPayload.class);
            skierDataMap.computeIfAbsent(payload.getSkierID(), k -> new ArrayList<>()).add(payload);
            long totalProcessed = messagesProcessed.incrementAndGet();
            logger.info("Total messages processed: {}", totalProcessed);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}


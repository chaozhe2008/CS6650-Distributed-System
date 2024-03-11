package com.zycao.multithreadTask;
import com.zycao.util.TaskAnalyzer;

import java.util.concurrent.*;

public class DynamicTaskPerformer extends TaskPerformer{
    public DynamicTaskPerformer(int eventQueueCapacity, int numEvents, int initialThreadPoolSize, int maxThreadPoolSize) {
        super(eventQueueCapacity, numEvents, initialThreadPoolSize, maxThreadPoolSize);
        this.executor = new DynamicThreadPoolExecutor(
                initialThreadPoolSize,
                maxThreadPoolSize);
    }

    public void performTasks() throws InterruptedException {
        String csvFileName = TaskPerformer.getCsvFileName();
        System.out.println("Logging to CSV file: " + csvFileName);
        long startTime = System.currentTimeMillis();
        Thread producerThread = new Thread(new EventProducer(queue, numEvents));
        producerThread.start();

        for (int i = 0; i < initialThreadPoolSize; i++) {
            executor.submit(new EventConsumer(csvFileName, httpClient, queue, counter, initialLatch, totalRequestsLatch));
        }
        initialLatch.await(); // Wait for the first thread to reach 1000 posts

        executor.adjustPoolSize();
        while (executor.getActiveCount() < maxThreadPoolSize && totalRequestsLatch.getCount() > 0) {
            executor.submit(new EventConsumer(csvFileName, httpClient, queue, counter, new CountDownLatch(0), totalRequestsLatch));
        }
        totalRequestsLatch.await(); // Wait for all requests finished

        executor.shutdown();
        try {
            if (!executor.awaitTermination(20, TimeUnit.SECONDS)) {
                System.out.println("Forcefully Shut Down");
                executor.shutdownNow();
            }
        } catch (InterruptedException ie) {
            executor.shutdownNow();
            Thread.currentThread().interrupt();
        }

        long endTime = System.currentTimeMillis();
        TaskAnalyzer.processAnalytics(csvFileName);
        printResults(startTime, endTime);
    }


    public static void main(String[] args) throws InterruptedException {
        int eventQueueCapacity = 50000;
        int numEvents = 200000;
        int initialThreadPoolSize = 32;
        int maxThreadPoolSize = 8;
        DynamicTaskPerformer performer = new DynamicTaskPerformer(eventQueueCapacity, numEvents, initialThreadPoolSize, maxThreadPoolSize);
        performer.performTasks();
    }
}

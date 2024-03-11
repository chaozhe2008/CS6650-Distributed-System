package com.zycao.multithreadTask;
import java.util.concurrent.*;

public class DynamicTaskPerformer extends TaskPerformer{
    private final int firstPhaseRequests;

    public DynamicTaskPerformer(int eventQueueCapacity, int numEvents, int initialThreadPoolSize, int maxThreadPoolSize, int firstPhaseRequests) {
        super(eventQueueCapacity, numEvents, initialThreadPoolSize, maxThreadPoolSize);
        this.executor = new DynamicThreadPoolExecutor(
                initialThreadPoolSize,
                maxThreadPoolSize);
        this.firstPhaseRequests = firstPhaseRequests;
    }

    public void performTasks() throws InterruptedException {
        long startTime = System.currentTimeMillis();
        Thread producerThread = new Thread(new EventProducer(queue, numEvents));
        producerThread.start();

        for (int i = 0; i < initialThreadPoolSize; i++) {
            executor.submit(new EventConsumer(httpClient, queue, counter, initialLatch, totalRequestsLatch, firstPhaseRequests));
        }
        initialLatch.await(); // Wait for the first thread to reach 1000 posts
        System.out.println("Phase 1 finished");
        executor.adjustPoolSize();
        while (executor.getActiveCount() < maxThreadPoolSize && totalRequestsLatch.getCount() > 0) {
            executor.submit(new EventConsumer(httpClient, queue, counter, new CountDownLatch(0), totalRequestsLatch, firstPhaseRequests));
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
        printResults(startTime, endTime);
    }


    public static void main(String[] args) throws InterruptedException {
        int eventQueueCapacity = 2000;
        int numEvents = 200000;
        int firstPhaseRequests = 1000;
        int initialThreadPoolSize = 32;
        int maxThreadPoolSize = 32;
        DynamicTaskPerformer performer = new DynamicTaskPerformer(eventQueueCapacity, numEvents, initialThreadPoolSize, maxThreadPoolSize,firstPhaseRequests);
        performer.performTasks();
    }

}

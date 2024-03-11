package com.zycao.multithreadTask;
import com.zycao.model.SkierLiftRideEvent;
import lombok.Getter;

import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.http.HttpClient;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.concurrent.*;

public class TaskPerformer {
    public int eventQueueCapacity;
    public int numEvents;
    public int initialThreadPoolSize;
    public int maxThreadPoolSize;
    public RequestCounter counter = new RequestCounter();
    public CountDownLatch initialLatch;
    public CountDownLatch totalRequestsLatch;
    public HttpClient httpClient;
    public DynamicThreadPoolExecutor executor;
    public BlockingQueue<SkierLiftRideEvent> queue;

    private static final DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyyMMddHHmmss");
    private static final String CSV_FILE_PREFIX = "./log_";
    @Getter
    private static String csvFileName;

    public TaskPerformer(int eventQueueCapacity, int numEvents, int initialThreadPoolSize, int maxThreadPoolSize) {
        this.eventQueueCapacity = eventQueueCapacity;
        this.numEvents = numEvents;
        this.initialThreadPoolSize = initialThreadPoolSize;
        this.maxThreadPoolSize = maxThreadPoolSize;
        this.initialLatch = new CountDownLatch(1);
        this.totalRequestsLatch = new CountDownLatch(numEvents);
        this.httpClient = HttpClient.newHttpClient();
        this.queue = new LinkedBlockingQueue<>(eventQueueCapacity);
        initializeCSV();
    }

    private void initializeCSV() {
        csvFileName = CSV_FILE_PREFIX + dtf.format(LocalDateTime.now()) + ".csv";
        try {
            if (!Files.exists(Paths.get(csvFileName))) {
                try (PrintWriter out = new PrintWriter(new FileWriter(csvFileName))) {
                    out.println("StartTime,RequestType,Latency(ms),ResponseCode");
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void printResults(long startTime, long endTime) {
        double totalTime = (endTime - startTime) / 1000.0;
        double throughput = numEvents / totalTime;
        System.out.println("--------------------------------");
        System.out.println("Event queue capacity : " + this.eventQueueCapacity);
        System.out.println("Max number of Threads : " + this.maxThreadPoolSize);
        System.out.println("Total run time: " + totalTime + " s");
        System.out.println("Total throughput: " + throughput + " requests/s");
        System.out.println("Total successful requests: " + counter.getSuccessfulRequests());
        System.out.println("Total failed requests: " + counter.getFailedRequests());
        System.out.println("--------------------------------");
    }
}

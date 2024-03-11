package com.zycao.multithreadTask;

import com.zycao.model.SkierLiftRideEvent;
import com.zycao.util.ParamGenerator;

import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.concurrent.*;

class EventConsumer implements Runnable {
    private final BlockingQueue<SkierLiftRideEvent> queue;
    private final RequestCounter counter;
    private final CountDownLatch initialLatch;
    private final CountDownLatch totalRequestsLatch;
    private final int MAX_ATTEMPT = 5;
    private int requestCount = 0;
    private final int MAX_REQUESTS = 1000;
    private final HttpClient httpClient;
    String csvFileName;


    EventConsumer(String csvFileName, HttpClient httpClient, BlockingQueue<SkierLiftRideEvent> queue, RequestCounter counter, CountDownLatch initialLatch, CountDownLatch totalRequestsLatch) {
        this.httpClient = httpClient;
        this.queue = queue;
        this.counter = counter;
        this.initialLatch = initialLatch;
        this.totalRequestsLatch = totalRequestsLatch;
        this.csvFileName = csvFileName;
    }

    private Integer sendPostRequest(SkierLiftRideEvent event) throws IOException{
        String targetUrl = ParamGenerator.parseEvent(event);
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(targetUrl))
                .header("Accept", "application/json")
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(""))
                .timeout(Duration.ofSeconds(100))
                .build();
        long startTime = System.currentTimeMillis();
        CompletableFuture<HttpResponse<String>> responseFuture = httpClient.sendAsync(request, HttpResponse.BodyHandlers.ofString());


        try {
            HttpResponse<String> response = responseFuture.get(10, TimeUnit.SECONDS);
            long endTime = System.currentTimeMillis();
            long latency = endTime - startTime;

            synchronized (EventConsumer.class) {
                try (PrintWriter out = new PrintWriter(new FileWriter(csvFileName, true))) {
                    out.printf("%d,POST,%d,%d%n", startTime, latency, response.statusCode());
                }
            }

            if (response.statusCode() == 200 || response.statusCode() == 201) {
                //System.out.println("Success: " + response.body());
            } else {
                System.out.println("Error: " + response.statusCode() + ", " + response.body());
                throw new RuntimeException("Error: " + response.statusCode() + ", " + response.body());
            }
            return response.statusCode();
        } catch (TimeoutException e) {
            System.out.println("Request timed out. Shutting down the system.");
            System.exit(1);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        } catch (ExecutionException e) {
            System.out.println("Error during HTTP call: " + e.getMessage());
        }
        return null;
    }


    @Override
    public void run() {
        while (totalRequestsLatch.getCount() > 0) {
            try {
                SkierLiftRideEvent event = queue.poll(100, TimeUnit.MILLISECONDS);
                if (event == null) {
                    continue;
                }

                boolean success = false;
                for (int attempt = 0; attempt < MAX_ATTEMPT; attempt++) {
                    try {
                        Integer responseCode = sendPostRequest(event);
                        if (responseCode == 201) {
                            counter.incrementSuccessCount();
                            success = true;
                            break;
                        }
                    } catch (Exception retryException) {
                        Thread.sleep(100);
                    }
                }
                if (!success) {
                    counter.incrementFailCount();
                }
                totalRequestsLatch.countDown();
                if (++requestCount >= MAX_REQUESTS) {
                    initialLatch.countDown();
                }
            } catch (Exception e) {
                counter.incrementFailCount();
                Thread.currentThread().interrupt();
                System.out.println("Error during API call: " + e.getMessage());
            }
        }
    }

}

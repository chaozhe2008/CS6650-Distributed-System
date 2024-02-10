package com.zycao.client;

import com.zycao.model.SkierLiftRideEvent;
import com.zycao.util.ParamGenerator;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

public class LatencyTestClient {

    private final HttpClient httpClient;
    public LatencyTestClient() {
        this.httpClient = HttpClient.newHttpClient();
    }
    public void sendPostRequests(int numberOfRequests) throws IOException, InterruptedException {
        long totalDuration = 0;

        for (int i = 0; i < numberOfRequests; i++) {
            long prepareStartTime = System.currentTimeMillis();
            SkierLiftRideEvent event = ParamGenerator.generateRandomEvent();
            String url = ParamGenerator.parseEvent(event);
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(url))
                    .header("Accept", "application/json")
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(""))
                    .build();
            long startTime = System.currentTimeMillis();
            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            long endTime = System.currentTimeMillis();
            totalDuration += (endTime - startTime);

        }
        double averageLatency = (totalDuration / numberOfRequests);
        System.out.println("Average latency time for " + numberOfRequests + " requests: " + averageLatency + " ms");
    }

    public static void main(String[] args) {
        LatencyTestClient client = new LatencyTestClient();
        try {
            client.sendPostRequests(1000); // Removed requestBodyJson from arguments
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }
    }
}

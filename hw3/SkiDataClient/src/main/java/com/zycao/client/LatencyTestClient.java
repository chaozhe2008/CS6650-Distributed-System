package com.zycao.client;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.zycao.model.SkierLiftRideEvent;
import com.zycao.model.SkierRequestBody;
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
            SkierLiftRideEvent event = ParamGenerator.generateRandomEvent();
            String url = ParamGenerator.parseEvent(event);
            ObjectMapper objectMapper = new ObjectMapper();
            String json = objectMapper.writeValueAsString(new SkierRequestBody(event.getTime(), event.getLiftID()));
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(url))
                    .header("Accept", "application/json")
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(json))
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
            client.sendPostRequests(10);
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }
    }
}

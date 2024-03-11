package com.zycao.util;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class TaskAnalyzer {
    public static void processAnalytics(String csvFilePath) {
        List<Long> responseTimes = new ArrayList<>();
        try (BufferedReader br = new BufferedReader(new FileReader(csvFilePath))) {
            String line;
            br.readLine();
            while ((line = br.readLine()) != null) {
                String[] values = line.split(",");
                long latency = Long.parseLong(values[2]);
                responseTimes.add(latency);
            }
        } catch (IOException e) {
            e.printStackTrace();
            return;
        }

        if (responseTimes.isEmpty()) {
            System.out.println("No data to process.");
            return;
        }

        Collections.sort(responseTimes);
        double mean = responseTimes.stream().mapToLong(val -> val).average().orElse(0.0);
        double median = responseTimes.get(responseTimes.size() / 2);
        long p99 = responseTimes.get((int) (responseTimes.size() * 0.99) - 1);
        long min = responseTimes.get(0);
        long max = responseTimes.get(responseTimes.size() - 1);

        System.out.println("Mean response time (ms): " + mean);
        System.out.println("Median response time (ms): " + median);
        System.out.println("99th percentile response time (ms): " + p99);
        System.out.println("Min response time (ms): " + min);
        System.out.println("Max response time (ms): " + max);
    }
}

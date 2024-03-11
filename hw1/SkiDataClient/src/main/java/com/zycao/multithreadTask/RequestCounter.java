package com.zycao.multithreadTask;

import java.util.concurrent.atomic.AtomicInteger;

public class RequestCounter {
    private final AtomicInteger successfulRequests = new AtomicInteger(0);
    private final AtomicInteger failedRequests = new AtomicInteger(0);

    public void incrementSuccessCount() {
        successfulRequests.incrementAndGet();
    }

    public void incrementFailCount() {
        failedRequests.incrementAndGet();
    }

    public int getSuccessfulRequests() {
        return successfulRequests.get();
    }

    public int getFailedRequests() {
        return failedRequests.get();
    }
}

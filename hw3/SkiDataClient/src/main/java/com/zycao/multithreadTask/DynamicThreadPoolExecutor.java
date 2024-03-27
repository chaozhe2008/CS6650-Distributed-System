package com.zycao.multithreadTask;

import com.zycao.handler.RetryRejectedExecutionHandler;

import java.util.concurrent.*;

public class DynamicThreadPoolExecutor extends ThreadPoolExecutor {
    public int maximumPoolSize;

    public DynamicThreadPoolExecutor(int corePoolSize, int maximumPoolSize) {
        super(corePoolSize, corePoolSize, 60L, TimeUnit.SECONDS, new SynchronousQueue<>(), new RetryRejectedExecutionHandler(50));
        this.maximumPoolSize = maximumPoolSize;
    }

    // Method to adjust the pool size and queue capacity dynamically
    public void adjustPoolSize() {
        if (getCorePoolSize() >= maximumPoolSize){
            setCorePoolSize(maximumPoolSize);
        }
        setMaximumPoolSize(maximumPoolSize);
    }
}

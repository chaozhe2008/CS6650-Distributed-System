package com.zycao.multithreadTask;

import com.zycao.model.SkierLiftRideEvent;
import com.zycao.util.ParamGenerator;

import java.util.concurrent.BlockingQueue;

class EventProducer implements Runnable {
    private final BlockingQueue<SkierLiftRideEvent> queue;
    private final int numEvents;

    EventProducer(BlockingQueue<SkierLiftRideEvent> queue, int numEvents) {
        this.queue = queue;
        this.numEvents = numEvents;
    }

    @Override
    public void run() {
        for (int i = 0; i < numEvents; i++) {
            SkierLiftRideEvent event = ParamGenerator.generateRandomEvent();
            try {
                queue.put(event);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
    }
}
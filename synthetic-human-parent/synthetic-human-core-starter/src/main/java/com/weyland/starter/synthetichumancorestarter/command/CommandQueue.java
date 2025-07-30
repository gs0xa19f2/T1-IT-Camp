package com.weyland.starter.synthetichumancorestarter.command;

import java.util.Queue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.atomic.AtomicInteger;

public class CommandQueue {
    private final BlockingQueue<Command> queue;
    private final int maxSize;
    private final AtomicInteger executedCommands = new AtomicInteger(0);

    public CommandQueue(int maxSize) {
        this.maxSize = maxSize;
        this.queue = new LinkedBlockingQueue<>(maxSize);
    }

    public void add(Command command) {
        if (!queue.offer(command)) {
            throw new IllegalStateException("Command queue is full.");
        }
    }

    public Command poll() {
        return queue.poll();
    }

    public Command take() throws InterruptedException {
        return queue.take();
    }

    public int size() {
        return queue.size();
    }

    public int getExecutedCommands() {
        return executedCommands.get();
    }

    public void incrementExecuted() {
        executedCommands.incrementAndGet();
    }

    public Queue<Command> getCurrentQueue() {
        return queue;
    }

    public int getMaxSize() {
        return maxSize;
    }
}
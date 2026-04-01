package com.example.banking.service;

import org.springframework.stereotype.Component;

import java.util.Comparator;
import java.util.PriorityQueue;
import java.util.concurrent.atomic.AtomicInteger;

@Component
public class TransactionProcessor {
    private final PriorityQueue<QueuedTransaction> queue = new PriorityQueue<>(Comparator.comparingInt(QueuedTransaction::priority));
    private final AtomicInteger pendingCount = new AtomicInteger(0);

    public synchronized void submit(String transactionId, int priority) {
        queue.offer(new QueuedTransaction(transactionId, priority));
        pendingCount.incrementAndGet();
    }

    public synchronized String next() {
        QueuedTransaction next = queue.poll();
        if (next == null) {
            return null;
        }
        pendingCount.decrementAndGet();
        return next.transactionId();
    }

    public int pendingCount() {
        return pendingCount.get();
    }

    private record QueuedTransaction(String transactionId, int priority) {}
}

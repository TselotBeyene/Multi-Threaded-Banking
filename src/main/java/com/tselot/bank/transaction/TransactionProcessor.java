package com.tselot.bank.transaction;

import java.util.PriorityQueue;

public final class TransactionProcessor {
    private final PriorityQueue<Transaction> queue = new PriorityQueue<>();

    public synchronized void submit(Transaction transaction) {
        queue.offer(transaction);
    }

    public synchronized boolean hasPending() {
        return !queue.isEmpty();
    }

    public synchronized Transaction next() {
        return queue.poll();
    }
}

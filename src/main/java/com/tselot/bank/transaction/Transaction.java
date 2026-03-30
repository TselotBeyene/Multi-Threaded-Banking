package com.tselot.bank.transaction;

import com.tselot.bank.model.TransactionResult;
import com.tselot.bank.service.Bank;

import java.time.Instant;
import java.util.UUID;

public abstract class Transaction implements Comparable<Transaction> {
    private final String id = UUID.randomUUID().toString();
    private final int priority;
    private final Instant createdAt = Instant.now();
    private final String description;

    protected Transaction(int priority, String description) {
        this.priority = priority;
        this.description = description;
    }

    public String id() {
        return id;
    }

    public int priority() {
        return priority;
    }

    public Instant createdAt() {
        return createdAt;
    }

    public String description() {
        return description;
    }

    public abstract TransactionResult execute(Bank bank, long sequence);

    @Override
    public int compareTo(Transaction other) {
        int byPriority = Integer.compare(this.priority, other.priority);
        return byPriority != 0 ? byPriority : this.createdAt.compareTo(other.createdAt);
    }
}

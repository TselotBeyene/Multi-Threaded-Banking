package com.tselot.bank.account;

import com.tselot.bank.exception.InsufficientFundsException;
import com.tselot.bank.exception.InvalidTransactionException;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.ReentrantLock;

public abstract class Account {
    private final String accountNumber;
    private final String ownerName;
    protected final BigDecimal minimumBalance;
    protected final ReentrantLock lock = new ReentrantLock(true);
    protected final AtomicInteger transactionCount = new AtomicInteger();
    protected BigDecimal balance;

    protected Account(String accountNumber, String ownerName, BigDecimal openingBalance, BigDecimal minimumBalance) {
        this.accountNumber = Objects.requireNonNull(accountNumber);
        this.ownerName = Objects.requireNonNull(ownerName);
        this.balance = normalize(openingBalance);
        this.minimumBalance = normalize(minimumBalance);
    }

    public String accountNumber() {
        return accountNumber;
    }

    public String ownerName() {
        return ownerName;
    }

    public int transactionCount() {
        return transactionCount.get();
    }

    public BigDecimal balanceSnapshot() {
        lock.lock();
        try {
            return balance;
        } finally {
            lock.unlock();
        }
    }

    public void deposit(BigDecimal amount) {
        BigDecimal normalized = requirePositive(amount);
        lock.lock();
        try {
            balance = balance.add(normalized);
            transactionCount.incrementAndGet();
        } finally {
            lock.unlock();
        }
    }

    public void withdraw(BigDecimal amount) {
        BigDecimal normalized = requirePositive(amount);
        lock.lock();
        try {
            if (!canWithdraw(normalized)) {
                throw new InsufficientFundsException("Insufficient funds in " + accountNumber + " for amount " + normalized);
            }
            balance = balance.subtract(normalized);
            transactionCount.incrementAndGet();
        } finally {
            lock.unlock();
        }
    }

    public boolean canWithdraw(BigDecimal amount) {
        BigDecimal normalized = normalize(amount);
        return balance.subtract(normalized).compareTo(minimumBalance) >= 0;
    }

    public ReentrantLock lock() {
        return lock;
    }

    protected BigDecimal requirePositive(BigDecimal amount) {
        BigDecimal normalized = normalize(amount);
        if (normalized.compareTo(BigDecimal.ZERO) <= 0) {
            throw new InvalidTransactionException("Amount must be positive");
        }
        return normalized;
    }

    protected BigDecimal normalize(BigDecimal value) {
        return value.setScale(2, RoundingMode.HALF_EVEN);
    }

    @Override
    public String toString() {
        return getClass().getSimpleName() + "{" +
                "accountNumber='" + accountNumber + '\'' +
                ", ownerName='" + ownerName + '\'' +
                ", balance=" + balanceSnapshot() +
                ", minimumBalance=" + minimumBalance +
                ", transactionCount=" + transactionCount() +
                '}';
    }
}

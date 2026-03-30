package com.tselot.bank.service;

import com.tselot.bank.account.Account;
import com.tselot.bank.exception.InsufficientFundsException;
import com.tselot.bank.exception.InvalidTransactionException;
import com.tselot.bank.model.TransactionResult;
import com.tselot.bank.model.TransactionStatus;
import com.tselot.bank.session.Session;
import com.tselot.bank.transaction.Transaction;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

public final class Bank {
    private final String name;
    private final ConcurrentHashMap<String, Account> accounts = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<String, Session> activeSessions = new ConcurrentHashMap<>();
    private final List<TransactionResult> auditTrail = java.util.Collections.synchronizedList(new ArrayList<>());
    private final AtomicLong sequence = new AtomicLong(1000);
    private final TransferService transferService = new TransferService();

    public Bank(String name) {
        this.name = Objects.requireNonNull(name);
    }

    public void registerAccount(Account account) {
        accounts.put(account.accountNumber(), account);
    }

    public Map<String, Account> accounts() {
        return accounts;
    }

    public Map<String, Session> activeSessions() {
        return activeSessions;
    }

    public List<TransactionResult> auditTrail() {
        return List.copyOf(auditTrail);
    }

    public synchronized String login(String ownerName) {
        String sessionId = UUID.randomUUID().toString();
        activeSessions.put(sessionId, new Session(sessionId, ownerName, Instant.now()));
        return sessionId;
    }

    public void logout(String sessionId) {
        activeSessions.remove(sessionId);
    }

    public TransactionResult process(Transaction transaction) {
        long next = sequence.incrementAndGet();
        try {
            TransactionResult result = transaction.execute(this, next);
            auditTrail.add(result);
            return result;
        } catch (InvalidTransactionException | InsufficientFundsException e) {
            TransactionResult result = new TransactionResult(transaction.id(), next, TransactionStatus.FAILED, e.getMessage(), Instant.now());
            auditTrail.add(result);
            return result;
        } catch (Exception e) {
            TransactionResult result = new TransactionResult(transaction.id(), next, TransactionStatus.FAILED, "Unexpected error: " + e.getMessage(), Instant.now());
            auditTrail.add(result);
            return result;
        }
    }

    public Account requireAccount(String accountNumber) {
        Account account = accounts.get(accountNumber);
        if (account == null) {
            throw new InvalidTransactionException("Account not found: " + accountNumber);
        }
        return account;
    }

    public TransferService transferService() {
        return transferService;
    }

    @Override
    public String toString() {
        return "Bank{" + "name='" + name + '\'' + '}';
    }
}

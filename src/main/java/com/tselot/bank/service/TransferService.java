package com.tselot.bank.service;

import com.tselot.bank.account.Account;
import com.tselot.bank.exception.InvalidTransactionException;

import java.math.BigDecimal;
import java.util.concurrent.locks.ReentrantLock;

public final class TransferService {
    public void transfer(Account from, Account to, BigDecimal amount) {
        if (from.accountNumber().equals(to.accountNumber())) {
            throw new InvalidTransactionException("Cannot transfer to the same account");
        }

        Account first = from.accountNumber().compareTo(to.accountNumber()) < 0 ? from : to;
        Account second = first == from ? to : from;

        ReentrantLock firstLock = first.lock();
        ReentrantLock secondLock = second.lock();

        firstLock.lock();
        secondLock.lock();
        try {
            from.withdraw(amount);
            to.deposit(amount);
        } finally {
            secondLock.unlock();
            firstLock.unlock();
        }
    }
}

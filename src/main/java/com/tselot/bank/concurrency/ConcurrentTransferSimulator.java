package com.tselot.bank.concurrency;

import com.tselot.bank.account.Account;
import com.tselot.bank.service.Bank;
import com.tselot.bank.transaction.TransferTransaction;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

public final class ConcurrentTransferSimulator {
    private final Bank bank;

    public ConcurrentTransferSimulator(Bank bank) {
        this.bank = bank;
    }

    public void run(Account a, Account b, Account c, int rounds) throws Exception {
        try (ExecutorService executor = Executors.newFixedThreadPool(6)) {
            List<Future<?>> futures = new ArrayList<>();
            for (int i = 0; i < rounds; i++) {
                futures.add(executor.submit(() -> System.out.println(bank.process(
                        new TransferTransaction(1, a.accountNumber(), b.accountNumber(), new BigDecimal("50.00"), "Concurrent A->B")))));
                futures.add(executor.submit(() -> System.out.println(bank.process(
                        new TransferTransaction(1, b.accountNumber(), c.accountNumber(), new BigDecimal("30.00"), "Concurrent B->C")))));
                futures.add(executor.submit(() -> System.out.println(bank.process(
                        new TransferTransaction(1, c.accountNumber(), a.accountNumber(), new BigDecimal("20.00"), "Concurrent C->A")))));
            }
            for (Future<?> future : futures) {
                future.get();
            }
        }
    }
}

package com.tselot.bank.service;

import com.tselot.bank.account.Account;
import com.tselot.bank.account.CheckingAccount;
import com.tselot.bank.account.SavingsAccount;
import com.tselot.bank.model.TransactionStatus;
import com.tselot.bank.transaction.TransferTransaction;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class BankConcurrencyTest {

    @Test
    void concurrentTransfersPreserveTotalBalance() throws Exception {
        Bank bank = new Bank("Test Bank");
        Account a = new SavingsAccount("A", "Alice", new BigDecimal("5000.00"), new BigDecimal("100.00"), 0.02);
        Account b = new CheckingAccount("B", "Bob", new BigDecimal("2500.00"), new BigDecimal("500.00"), new BigDecimal("500.00"));
        Account c = new CheckingAccount("C", "Carol", new BigDecimal("1500.00"), new BigDecimal("300.00"), new BigDecimal("300.00"));
        bank.registerAccount(a);
        bank.registerAccount(b);
        bank.registerAccount(c);

        BigDecimal initialTotal = a.balanceSnapshot().add(b.balanceSnapshot()).add(c.balanceSnapshot());

        try (ExecutorService executor = Executors.newFixedThreadPool(6)) {
            List<Future<?>> futures = new ArrayList<>();
            for (int i = 0; i < 20; i++) {
                futures.add(executor.submit(() -> bank.process(new TransferTransaction(1, "A", "B", new BigDecimal("50.00"), "A->B"))));
                futures.add(executor.submit(() -> bank.process(new TransferTransaction(1, "B", "C", new BigDecimal("30.00"), "B->C"))));
                futures.add(executor.submit(() -> bank.process(new TransferTransaction(1, "C", "A", new BigDecimal("20.00"), "C->A"))));
            }
            for (Future<?> future : futures) {
                future.get();
            }
        }

        BigDecimal finalTotal = a.balanceSnapshot().add(b.balanceSnapshot()).add(c.balanceSnapshot());
        assertEquals(0, initialTotal.compareTo(finalTotal));
        assertTrue(bank.auditTrail().stream().allMatch(r -> r.status() == TransactionStatus.SUCCESS));
    }
}

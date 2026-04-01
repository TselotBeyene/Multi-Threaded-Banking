package com.tselot.bank;

import com.tselot.bank.account.Account;
import com.tselot.bank.account.CheckingAccount;
import com.tselot.bank.account.SavingsAccount;
import com.tselot.bank.concurrency.ConcurrentTransferSimulator;
import com.tselot.bank.gateway.MockPaymentGateway;
import com.tselot.bank.gateway.PaymentGateway;
import com.tselot.bank.model.TransactionResult;
import com.tselot.bank.report.ReportingEngine;
import com.tselot.bank.service.Bank;
import com.tselot.bank.transaction.DepositTransaction;
import com.tselot.bank.transaction.GatewayPaymentTransaction;
import com.tselot.bank.transaction.Transaction;
import com.tselot.bank.transaction.TransactionProcessor;
import com.tselot.bank.transaction.TransferTransaction;
import com.tselot.bank.transaction.WithdrawTransaction;

import java.math.BigDecimal;
import java.util.List;

public final class BankingApplication {
    private BankingApplication() {}

    public static void main(String[] args) throws Exception {
        Bank bank = new Bank("Apex Digital Bank");
        PaymentGateway gateway = new MockPaymentGateway();

        Account aliceSavings = new SavingsAccount("ACC-1001", "Alice", money("5000.00"), money("100.00"), 0.025);
        Account bobChecking = new CheckingAccount("ACC-1002", "Bob", money("2500.00"), money("500.00"), money("750.00"));
        Account carolChecking = new CheckingAccount("ACC-1003", "Carol", money("1500.00"), money("300.00"), money("300.00"));

        bank.registerAccount(aliceSavings);
        bank.registerAccount(bobChecking);
        bank.registerAccount(carolChecking);

        String aliceSession = bank.login("Alice");
        String bobSession = bank.login("Bob");
        String carolSession = bank.login("Carol");

        System.out.println("=== ACTIVE SESSIONS ===");
        bank.activeSessions().forEach((id, session) -> System.out.println(id + " -> " + session));

        TransactionProcessor processor = new TransactionProcessor();
        List<Transaction> scheduled = List.of(
                new TransferTransaction(1, aliceSavings.accountNumber(), bobChecking.accountNumber(), money("750.00"), "Rent payment"),
                new TransferTransaction(2, bobChecking.accountNumber(), carolChecking.accountNumber(), money("200.00"), "Family support"),
                new WithdrawTransaction(3, bobChecking.accountNumber(), money("120.00"), "ATM cash"),
                new DepositTransaction(4, aliceSavings.accountNumber(), money("400.00"), "Bonus credit"),
                new GatewayPaymentTransaction(5, carolChecking.accountNumber(), money("89.99"), "Utility bill", gateway)
        );
        scheduled.forEach(processor::submit);

        System.out.println();
        System.out.println("=== PRIORITY TRANSACTION EXECUTION ===");
        while (processor.hasPending()) {
            TransactionResult result = bank.process(processor.next());
            System.out.println(result);
        }

        System.out.println();
        System.out.println("=== CONCURRENT TRANSFER SIMULATION ===");
        new ConcurrentTransferSimulator(bank).run(aliceSavings, bobChecking, carolChecking, 8);

        System.out.println();
        System.out.println("=== FINAL BALANCES ===");
        bank.accounts().values().stream()
                .sorted((a, b) -> a.accountNumber().compareTo(b.accountNumber()))
                .forEach(System.out::println);

        System.out.println();
        System.out.println("=== MEMORY AND CONCURRENCY NOTES ===");
        System.out.println("1. synchronized login creates visibility and serialization for session creation.");
        System.out.println("2. ReentrantLock guards mutable balances and transaction counts.");
        System.out.println("3. Atomic counters provide lock-free, thread-safe sequencing and metrics.");
        System.out.println("4. ConcurrentHashMap safely stores active sessions and accounts.");
        System.out.println("5. Lock ordering by account number prevents transfer deadlocks.");

        ReportingEngine<Account> accountReporting = new ReportingEngine<>();
        ReportingEngine<TransactionResult> resultReporting = new ReportingEngine<>();

        System.out.println();
        System.out.println("=== GENERIC ACCOUNT REPORT ===");
        System.out.println(accountReporting.generate(bank.accounts().values(),
                acc -> acc.accountNumber() + " | " + acc.ownerName() + " | " + acc.balanceSnapshot() + " | tx=" + acc.transactionCount()));

        System.out.println();
        System.out.println("=== GENERIC AUDIT REPORT ===");
        System.out.println(resultReporting.generate(bank.auditTrail(),
                res -> res.sequence() + " | " + res.status() + " | " + res.message()));

        bank.logout(aliceSession);
        bank.logout(bobSession);
        bank.logout(carolSession);
    }

    private static BigDecimal money(String amount) {
        return new BigDecimal(amount);
    }
}

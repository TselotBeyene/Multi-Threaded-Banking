package com.tselot.bank.transaction;

import com.tselot.bank.account.Account;
import com.tselot.bank.model.TransactionResult;
import com.tselot.bank.model.TransactionStatus;
import com.tselot.bank.service.Bank;

import java.math.BigDecimal;
import java.time.Instant;

public final class WithdrawTransaction extends Transaction {
    private final String accountNumber;
    private final BigDecimal amount;

    public WithdrawTransaction(int priority, String accountNumber, BigDecimal amount, String description) {
        super(priority, description);
        this.accountNumber = accountNumber;
        this.amount = amount;
    }

    @Override
    public TransactionResult execute(Bank bank, long sequence) {
        Account account = bank.requireAccount(accountNumber);
        account.withdraw(amount);
        return new TransactionResult(id(), sequence, TransactionStatus.SUCCESS,
                "Withdrew " + amount + " from " + accountNumber + " :: " + description(), Instant.now());
    }
}

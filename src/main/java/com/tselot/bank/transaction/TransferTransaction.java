package com.tselot.bank.transaction;

import com.tselot.bank.account.Account;
import com.tselot.bank.model.TransactionResult;
import com.tselot.bank.model.TransactionStatus;
import com.tselot.bank.service.Bank;

import java.math.BigDecimal;
import java.time.Instant;

public final class TransferTransaction extends Transaction {
    private final String fromAccount;
    private final String toAccount;
    private final BigDecimal amount;

    public TransferTransaction(int priority, String fromAccount, String toAccount, BigDecimal amount, String description) {
        super(priority, description);
        this.fromAccount = fromAccount;
        this.toAccount = toAccount;
        this.amount = amount;
    }

    @Override
    public TransactionResult execute(Bank bank, long sequence) {
        Account from = bank.requireAccount(fromAccount);
        Account to = bank.requireAccount(toAccount);
        bank.transferService().transfer(from, to, amount);
        return new TransactionResult(id(), sequence, TransactionStatus.SUCCESS,
                "Transferred " + amount + " from " + fromAccount + " to " + toAccount + " :: " + description(), Instant.now());
    }
}

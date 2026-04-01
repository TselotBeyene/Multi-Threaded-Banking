package com.tselot.bank.transaction;

import com.tselot.bank.account.Account;
import com.tselot.bank.gateway.PaymentGateway;
import com.tselot.bank.gateway.PaymentReceipt;
import com.tselot.bank.model.TransactionResult;
import com.tselot.bank.model.TransactionStatus;
import com.tselot.bank.service.Bank;

import java.math.BigDecimal;
import java.time.Instant;

public final class GatewayPaymentTransaction extends Transaction {
    private final String accountNumber;
    private final BigDecimal amount;
    private final PaymentGateway gateway;

    public GatewayPaymentTransaction(int priority, String accountNumber, BigDecimal amount, String description, PaymentGateway gateway) {
        super(priority, description);
        this.accountNumber = accountNumber;
        this.amount = amount;
        this.gateway = gateway;
    }

    @Override
    public TransactionResult execute(Bank bank, long sequence) {
        Account account = bank.requireAccount(accountNumber);
        account.withdraw(amount);
        PaymentReceipt receipt = gateway.charge(accountNumber, amount, description());
        return new TransactionResult(id(), sequence, TransactionStatus.SUCCESS,
                "Gateway payment succeeded: " + receipt.receiptId() + " :: " + description(), Instant.now());
    }
}

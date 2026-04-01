package com.tselot.bank.account;

import java.math.BigDecimal;

public final class CheckingAccount extends Account {
    private final BigDecimal overdraftLimit;

    public CheckingAccount(String accountNumber, String ownerName, BigDecimal openingBalance, BigDecimal minimumBalance, BigDecimal overdraftLimit) {
        super(accountNumber, ownerName, openingBalance, minimumBalance);
        this.overdraftLimit = normalize(overdraftLimit);
    }

    @Override
    public boolean canWithdraw(BigDecimal amount) {
        BigDecimal normalized = normalize(amount);
        BigDecimal floor = minimumBalance.subtract(overdraftLimit);
        return balance.subtract(normalized).compareTo(floor) >= 0;
    }

    public BigDecimal overdraftLimit() {
        return overdraftLimit;
    }
}

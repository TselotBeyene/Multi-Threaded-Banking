package com.tselot.bank.account;

import java.math.BigDecimal;

public final class SavingsAccount extends Account {
    private final double annualInterestRate;

    public SavingsAccount(String accountNumber, String ownerName, BigDecimal openingBalance, BigDecimal minimumBalance, double annualInterestRate) {
        super(accountNumber, ownerName, openingBalance, minimumBalance);
        this.annualInterestRate = annualInterestRate;
    }

    public double annualInterestRate() {
        return annualInterestRate;
    }

    public BigDecimal projectMonthlyInterest() {
        return balanceSnapshot().multiply(BigDecimal.valueOf(annualInterestRate / 12.0)).setScale(2, java.math.RoundingMode.HALF_EVEN);
    }
}

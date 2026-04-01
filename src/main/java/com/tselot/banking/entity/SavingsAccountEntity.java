package com.tselot.banking.entity;

import jakarta.persistence.Column;
import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;

import java.math.BigDecimal;

@Entity
@DiscriminatorValue("SAVINGS")
public class SavingsAccountEntity extends AccountEntity {
    @Column(name = "interest_rate", precision = 8, scale = 5)
    private BigDecimal interestRate;

    protected SavingsAccountEntity() {
    }

    public SavingsAccountEntity(String accountNumber, String ownerName, BigDecimal balance, BigDecimal minimumBalance, BigDecimal interestRate) {
        super(accountNumber, ownerName, balance, minimumBalance);
        this.interestRate = interestRate;
    }

    public BigDecimal getInterestRate() {
        return interestRate;
    }

    @Override
    public String getAccountType() {
        return "Savings";
    }

    @Override
    public boolean canWithdraw(BigDecimal amount) {
        return getBalance().subtract(amount).compareTo(getMinimumBalance()) >= 0;
    }
}

package com.tselot.banking.entity;

import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;

import java.math.BigDecimal;

@Entity
@DiscriminatorValue("CHECKING")
public class CheckingAccountEntity extends AccountEntity {
    protected CheckingAccountEntity() {
    }

    public CheckingAccountEntity(String accountNumber, String ownerName, BigDecimal balance, BigDecimal minimumBalance) {
        super(accountNumber, ownerName, balance, minimumBalance);
    }

    @Override
    public String getAccountType() {
        return "Checking";
    }

    @Override
    public boolean canWithdraw(BigDecimal amount) {
        return getBalance().subtract(amount).compareTo(getMinimumBalance().negate()) >= 0;
    }
}

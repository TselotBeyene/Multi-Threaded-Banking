package com.example.banking.entity;

import jakarta.persistence.Column;
import jakarta.persistence.DiscriminatorColumn;
import jakarta.persistence.DiscriminatorType;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Inheritance;
import jakarta.persistence.InheritanceType;
import jakarta.persistence.Table;
import jakarta.persistence.Version;

import java.math.BigDecimal;

@Entity
@Table(name = "accounts")
@Inheritance(strategy = InheritanceType.SINGLE_TABLE)
@DiscriminatorColumn(name = "account_type", discriminatorType = DiscriminatorType.STRING)
public abstract class AccountEntity {
    @Id
    @Column(name = "account_number", nullable = false, updatable = false, length = 30)
    private String accountNumber;

    @Column(name = "owner_name", nullable = false)
    private String ownerName;

    @Column(nullable = false, precision = 19, scale = 2)
    private BigDecimal balance;

    @Column(name = "minimum_balance", nullable = false, precision = 19, scale = 2)
    private BigDecimal minimumBalance;

    @Column(name = "transaction_count", nullable = false)
    private int transactionCount;

    @Version
    private long version;

    protected AccountEntity() {
    }

    protected AccountEntity(String accountNumber, String ownerName, BigDecimal balance, BigDecimal minimumBalance) {
        this.accountNumber = accountNumber;
        this.ownerName = ownerName;
        this.balance = balance;
        this.minimumBalance = minimumBalance;
        this.transactionCount = 0;
    }

    public String getAccountNumber() {
        return accountNumber;
    }

    public String getOwnerName() {
        return ownerName;
    }

    public BigDecimal getBalance() {
        return balance;
    }

    public void setBalance(BigDecimal balance) {
        this.balance = balance;
    }

    public BigDecimal getMinimumBalance() {
        return minimumBalance;
    }

    public int getTransactionCount() {
        return transactionCount;
    }

    public void incrementTransactionCount() {
        this.transactionCount++;
    }

    public abstract String getAccountType();

    public abstract boolean canWithdraw(BigDecimal amount);
}

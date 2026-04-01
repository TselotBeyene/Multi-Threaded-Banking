package com.tselot.banking.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "external_bank_accounts")
public class ExternalBankAccountEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "bank_name", nullable = false)
    private String bankName;

    @Column(name = "routing_code", nullable = false, length = 50)
    private String routingCode;

    @Column(name = "account_number", nullable = false, length = 30, unique = true)
    private String accountNumber;

    @Column(name = "owner_name", nullable = false)
    private String ownerName;

    @Column(nullable = false)
    private boolean active = true;

    protected ExternalBankAccountEntity() {
    }

    public ExternalBankAccountEntity(String bankName, String routingCode, String accountNumber, String ownerName) {
        this.bankName = bankName;
        this.routingCode = routingCode;
        this.accountNumber = accountNumber;
        this.ownerName = ownerName;
        this.active = true;
    }

    public Long getId() { return id; }
    public String getBankName() { return bankName; }
    public String getRoutingCode() { return routingCode; }
    public String getAccountNumber() { return accountNumber; }
    public String getOwnerName() { return ownerName; }
    public boolean isActive() { return active; }
    public void setActive(boolean active) { this.active = active; }
}

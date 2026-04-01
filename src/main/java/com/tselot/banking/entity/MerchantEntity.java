package com.tselot.banking.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "merchants")
public class MerchantEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "merchant_code", nullable = false, unique = true, length = 40)
    private String merchantCode;

    @Column(name = "merchant_name", nullable = false)
    private String merchantName;

    @Column(name = "settlement_reference", nullable = false)
    private String settlementReference;

    @Column(nullable = false)
    private boolean active = true;

    protected MerchantEntity() {
    }

    public MerchantEntity(String merchantCode, String merchantName, String settlementReference) {
        this.merchantCode = merchantCode;
        this.merchantName = merchantName;
        this.settlementReference = settlementReference;
        this.active = true;
    }

    public Long getId() { return id; }
    public String getMerchantCode() { return merchantCode; }
    public String getMerchantName() { return merchantName; }
    public String getSettlementReference() { return settlementReference; }
    public boolean isActive() { return active; }
    public void setActive(boolean active) { this.active = active; }
}

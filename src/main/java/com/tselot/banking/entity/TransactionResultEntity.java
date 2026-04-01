package com.tselot.banking.entity;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.time.Instant;

@Entity
@Table(name = "transaction_results")
public class TransactionResultEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long sequence;

    @Column(name = "transaction_id", nullable = false, updatable = false, length = 64)
    private String transactionId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private TransactionStatus status;

    @Column(nullable = false, length = 1000)
    private String message;

    @Column(name = "processed_at", nullable = false)
    private Instant processedAt;

    protected TransactionResultEntity() {
    }

    public TransactionResultEntity(String transactionId, TransactionStatus status, String message, Instant processedAt) {
        this.transactionId = transactionId;
        this.status = status;
        this.message = message;
        this.processedAt = processedAt;
    }

    @JsonProperty("sequence")
    public Long getSequence() {
        return sequence;
    }

    @JsonProperty("transactionId")
    public String getTransactionId() {
        return transactionId;
    }

    @JsonProperty("status")
    public TransactionStatus getStatus() {
        return status;
    }

    @JsonProperty("message")
    public String getMessage() {
        return message;
    }

    @JsonProperty("processedAt")
    public Instant getProcessedAt() {
        return processedAt;
    }
}

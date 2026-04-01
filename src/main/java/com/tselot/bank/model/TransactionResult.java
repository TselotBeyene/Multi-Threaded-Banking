package com.tselot.bank.model;

import java.time.Instant;

public record TransactionResult(String transactionId,
                                long sequence,
                                TransactionStatus status,
                                String message,
                                Instant completedAt) {}

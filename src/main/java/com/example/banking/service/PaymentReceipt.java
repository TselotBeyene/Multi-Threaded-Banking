package com.example.banking.service;

import java.time.Instant;

public record PaymentReceipt(
        String reference,
        Instant processedAt
) {}

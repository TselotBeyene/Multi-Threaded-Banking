package com.tselot.banking.service;

import java.time.Instant;

public record PaymentReceipt(
        String reference,
        Instant processedAt
) {}

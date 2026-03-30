package com.tselot.bank.gateway;

import java.time.Instant;

public record PaymentReceipt(String receiptId, String accountNumber, String reference, Instant processedAt) {}

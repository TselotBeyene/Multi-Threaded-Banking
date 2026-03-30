package com.tselot.bank.gateway;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public final class MockPaymentGateway implements PaymentGateway {
    @Override
    public PaymentReceipt charge(String accountNumber, BigDecimal amount, String reference) {
        return new PaymentReceipt(UUID.randomUUID().toString(), accountNumber, reference + " amount=" + amount, Instant.now());
    }
}

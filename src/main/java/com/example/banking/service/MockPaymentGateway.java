package com.example.banking.service;

import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Component
public class MockPaymentGateway implements PaymentGateway {
    @Override
    public PaymentReceipt pay(String accountNumber, BigDecimal amount, String merchant) {
        return new PaymentReceipt("PG-" + UUID.randomUUID(), Instant.now());
    }
}

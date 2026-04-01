package com.tselot.banking.service;

import java.math.BigDecimal;

public interface PaymentGateway {
    PaymentReceipt pay(String accountNumber, BigDecimal amount, String merchant);
}

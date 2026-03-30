package com.tselot.bank.gateway;

import java.math.BigDecimal;

public interface PaymentGateway {
    PaymentReceipt charge(String accountNumber, BigDecimal amount, String reference);
}

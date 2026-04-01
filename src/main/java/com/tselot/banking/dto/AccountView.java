package com.tselot.banking.dto;

import java.math.BigDecimal;

public record AccountView(
        String accountNumber,
        String ownerName,
        String accountType,
        BigDecimal balance,
        BigDecimal minimumBalance,
        int transactionCount
) {}

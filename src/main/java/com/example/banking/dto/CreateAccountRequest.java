package com.example.banking.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

public record CreateAccountRequest(
        @NotBlank String accountNumber,
        @NotBlank String ownerName,
        @NotBlank String accountType,
        @NotNull @DecimalMin("0.00") BigDecimal openingBalance,
        @NotNull @DecimalMin("0.00") BigDecimal minimumBalance,
        BigDecimal interestRate
) {}

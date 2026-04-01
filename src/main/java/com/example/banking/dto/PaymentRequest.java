package com.example.banking.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

public record PaymentRequest(
        @NotBlank String accountNumber,
        @NotNull Long merchantId,
        @NotNull @DecimalMin("0.01") BigDecimal amount,
        String description,
        Integer priority
) {}

package com.example.banking.dto;

import jakarta.validation.constraints.NotBlank;

public record CreateMerchantRequest(
        @NotBlank String merchantCode,
        @NotBlank String merchantName,
        String settlementReference
) {}

package com.tselot.banking.dto;

public record MerchantView(
        Long id,
        String merchantCode,
        String merchantName,
        String settlementReference,
        boolean active
) {}

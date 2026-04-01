package com.example.banking.dto;

public record ExternalAccountView(
        Long id,
        String bankName,
        String routingCode,
        String accountNumber,
        String ownerName,
        boolean active
) {}

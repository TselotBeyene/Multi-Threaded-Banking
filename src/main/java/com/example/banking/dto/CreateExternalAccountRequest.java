package com.example.banking.dto;

import jakarta.validation.constraints.NotBlank;

public record CreateExternalAccountRequest(
        @NotBlank String bankName,
        @NotBlank String routingCode,
        @NotBlank String accountNumber,
        @NotBlank String ownerName
) {}

package com.example.banking.dto;

import jakarta.validation.constraints.NotBlank;

public record LoginRequest(@NotBlank String ownerName) {}

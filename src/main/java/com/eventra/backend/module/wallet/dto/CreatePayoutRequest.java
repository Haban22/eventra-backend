package com.eventra.backend.module.wallet.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;
import java.util.UUID;

public record CreatePayoutRequest(
        @NotNull @DecimalMin(value = "0.01") BigDecimal amount,
        @NotNull UUID methodId,
        String notes
) {}

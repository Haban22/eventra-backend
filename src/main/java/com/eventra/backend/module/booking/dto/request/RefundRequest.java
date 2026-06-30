package com.eventra.backend.module.booking.dto.request;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

import java.util.UUID;

public record RefundRequest(
        @NotNull UUID bookingId,
        @NotNull @Min(1) int quantity,
        String reason
) {}
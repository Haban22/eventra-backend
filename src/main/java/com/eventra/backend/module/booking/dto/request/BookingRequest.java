package com.eventra.backend.module.booking.dto.request;

import com.eventra.backend.module.booking.enums.PaymentMethod;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

import java.util.UUID;

public record BookingRequest(
        @NotNull UUID eventId,
        @NotNull UUID ticketId,
        @NotNull @Min(1) @Max(10) int quantity,
        @NotNull PaymentMethod paymentMethod
) {}
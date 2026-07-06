package com.eventra.backend.module.booking.dto.request;

import com.eventra.backend.module.booking.enums.PaymentMethod;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.util.UUID;

public record PaymentRequest(
        @NotNull UUID bookingId,
        @NotNull PaymentMethod paymentMethod,
        String cardholderName,
        String cardNumber,
        String expiryDate,
        String cvv
) {}
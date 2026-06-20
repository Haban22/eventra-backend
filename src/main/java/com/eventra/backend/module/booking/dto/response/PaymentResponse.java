package com.eventra.backend.module.booking.dto.response;

import com.eventra.backend.module.booking.entity.Payment;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public record PaymentResponse(
        UUID id,
        UUID bookingId,
        BigDecimal amount,
        String currency,
        String paymentMethod,
        String status,
        String transactionId,
        Instant createdAt
) {
    public static PaymentResponse from(Payment p) {
        return new PaymentResponse(
                p.getId(),
                p.getBookingId(),
                p.getAmount() != null ? p.getAmount().getAmount() : null,
                p.getAmount() != null ? p.getAmount().getCurrency() : "EGP",
                p.getPaymentMethod().name(),
                p.getStatus().name(),
                p.getTransactionId(),
                p.getCreatedAt()
        );
    }
}
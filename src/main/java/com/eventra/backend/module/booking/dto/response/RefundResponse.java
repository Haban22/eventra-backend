package com.eventra.backend.module.booking.dto.response;

import com.eventra.backend.module.booking.entity.Refund;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public record RefundResponse(
        UUID id,
        UUID bookingId,
        UUID paymentId,
        BigDecimal amount,
        String currency,
        String status,
        String reason,
        Instant createdAt
) {
    public static RefundResponse from(Refund r) {
        return new RefundResponse(
                r.getId(),
                r.getBookingId(),
                r.getPaymentId(),
                r.getAmount() != null ? r.getAmount().getAmount() : null,
                r.getAmount() != null ? r.getAmount().getCurrency() : "EGP",
                r.getStatus().name(),
                r.getReason(),
                r.getCreatedAt()
        );
    }
}
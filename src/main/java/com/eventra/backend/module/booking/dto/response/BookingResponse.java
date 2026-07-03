package com.eventra.backend.module.booking.dto.response;

import com.eventra.backend.module.booking.entity.Booking;

import com.eventra.backend.module.booking.valueobject.BookingItem;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

public record BookingResponse(
        UUID id,
        UUID attendeeId,
        UUID eventId,
        List<BookingItem> items,
        BigDecimal totalAmount,
        String currency,
        String status,
        boolean checkedIn,
        Instant checkedInAt,
        Instant holdExpiresAt,
        String transactionId,
        Instant createdAt,
        String attendeeName,
        String attendeeEmail
) {
    public static BookingResponse from(Booking b) {
        return from(b, null, null);
    }

    public static BookingResponse from(Booking b, String attendeeName, String attendeeEmail) {
        return new BookingResponse(
                b.getId(),
                b.getAttendeeId(),
                b.getEventId(),
                b.getItems(),
                b.getTotalAmount() != null ? b.getTotalAmount().getAmount() : null,
                b.getTotalAmount() != null ? b.getTotalAmount().getCurrency() : "EGP",
                b.getStatus().name(),
                b.isCheckedIn(),
                b.getCheckedInAt(),
                b.getHoldExpiresAt(),
                b.getTransactionId(),
                b.getCreatedAt(),
                attendeeName,
                attendeeEmail
        );
    }
}
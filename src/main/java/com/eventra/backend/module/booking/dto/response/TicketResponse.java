package com.eventra.backend.module.booking.dto.response;

import com.eventra.backend.module.booking.entity.Ticket;

import java.math.BigDecimal;
import java.util.UUID;

public record TicketResponse(
        UUID id,
        UUID eventId,
        String ticketType,
        BigDecimal price,
        String currency,
        int totalAvailable,
        int sold,
        int available
) {
    public static TicketResponse from(Ticket t) {
        return new TicketResponse(
                t.getId(),
                t.getEventId(),
                t.getTicketType().name(),
                t.getPrice() != null ? t.getPrice().getAmount() : null,
                t.getPrice() != null ? t.getPrice().getCurrency() : "EGP",
                t.getTotalAvailable(),
                t.getSold(),
                t.getAvailable()
        );
    }
}
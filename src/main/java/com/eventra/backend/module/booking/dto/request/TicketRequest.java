package com.eventra.backend.module.booking.dto.request;

import com.eventra.backend.module.booking.enums.TicketType;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

public record TicketRequest(
        @NotNull TicketType ticketType,
        @NotNull @Min(0) BigDecimal price,
        @NotNull @Min(1) int totalAvailable
) {}
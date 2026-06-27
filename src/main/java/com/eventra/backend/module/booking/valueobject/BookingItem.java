package com.eventra.backend.module.booking.valueobject;

import jakarta.persistence.Embeddable;
import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

@Embeddable
@Getter
@Setter
public class BookingItem {

    private UUID ticketId;
    private int quantity;
}
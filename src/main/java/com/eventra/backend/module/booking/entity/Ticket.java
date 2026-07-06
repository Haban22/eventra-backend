package com.eventra.backend.module.booking.entity;

import com.eventra.backend.module.booking.enums.TicketType;
import com.eventra.backend.module.booking.valueobject.Money;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.JdbcType;
import org.hibernate.dialect.PostgreSQLEnumJdbcType;

import java.time.Instant;
import java.util.UUID;

@Getter
@Setter
@Entity
@Table(name = "tickets")
public class Ticket {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    // Plain UUID reference to events table
    @Column(name = "event_id", nullable = false)
    private UUID eventId;

    @Enumerated(EnumType.STRING)
    @JdbcType(PostgreSQLEnumJdbcType.class)
    @Column(name = "ticket_type", nullable = false, columnDefinition = "ticket_type")
    private TicketType ticketType;

    @Embedded
    @AttributeOverrides({
        @AttributeOverride(name = "amount",   column = @Column(name = "price_amount")),
        @AttributeOverride(name = "currency", column = @Column(name = "price_currency"))
    })
    private Money price;

    @Column(name = "total_available", nullable = false)
    private int totalAvailable;

    @Column(name = "sold", nullable = false)
    private int sold = 0;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    @PrePersist
    void prePersist() {
        Instant now = Instant.now();
        createdAt = now;
        updatedAt = now;
    }

    @PreUpdate
    void preUpdate() {
        updatedAt = Instant.now();
    }

    public boolean reserve(int quantity) {
        if ((totalAvailable - sold) >= quantity) {
            sold += quantity;
            return true;
        }
        return false;
    }

    public void release(int quantity) {
        sold = Math.max(0, sold - quantity);
    }

    public int getAvailable() {
        return totalAvailable - sold;
    }
}
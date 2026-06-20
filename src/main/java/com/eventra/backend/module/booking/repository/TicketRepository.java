package com.eventra.backend.module.booking.repository;

import com.eventra.backend.module.booking.entity.Ticket;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface TicketRepository extends JpaRepository<Ticket, UUID> {

    List<Ticket> findByEventId(UUID eventId);

    boolean existsByEventId(UUID eventId);
}
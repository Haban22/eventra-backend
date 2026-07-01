package com.eventra.backend.module.messaging.repository;

import com.eventra.backend.module.messaging.entity.EventMessage;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface EventMessageRepository extends JpaRepository<EventMessage, UUID> {
    List<EventMessage> findByEventIdOrderByCreatedAtAsc(UUID eventId);
}

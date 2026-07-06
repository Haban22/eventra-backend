package com.eventra.backend.module.calendar.repository;

import com.eventra.backend.module.calendar.entity.PersonalEvent;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface PersonalEventRepository extends JpaRepository<PersonalEvent, UUID> {
    List<PersonalEvent> findByUserIdOrderByDateAsc(UUID userId);
}

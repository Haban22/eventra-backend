package com.eventra.backend.module.event.repository;

import com.eventra.backend.module.event.entity.ScheduleItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface ScheduleItemRepository extends JpaRepository<ScheduleItem, UUID> {
    List<ScheduleItem> findByEventIdOrderByOrderIndexAsc(UUID eventId);
    void deleteByEventId(UUID eventId);
}

package com.eventra.backend.module.event.repository;

import com.eventra.backend.module.event.entity.EventApproval;
import com.eventra.backend.module.event.enums.ApprovalStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface EventApprovalRepository extends JpaRepository<EventApproval, UUID> {

    Optional<EventApproval> findByEventId(UUID eventId);

    List<EventApproval> findByAdminId(UUID adminId);

    List<EventApproval> findByStatus(ApprovalStatus status);

    boolean existsByEventId(UUID eventId);
}
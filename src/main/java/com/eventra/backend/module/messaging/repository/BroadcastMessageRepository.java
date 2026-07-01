package com.eventra.backend.module.messaging.repository;

import com.eventra.backend.module.messaging.entity.BroadcastMessage;
import com.eventra.backend.module.messaging.enums.BroadcastTargetRole;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface BroadcastMessageRepository extends JpaRepository<BroadcastMessage, UUID> {
    List<BroadcastMessage> findBySenderIdOrTargetRoleOrderByCreatedAtDesc(UUID senderId, BroadcastTargetRole targetRole);
}

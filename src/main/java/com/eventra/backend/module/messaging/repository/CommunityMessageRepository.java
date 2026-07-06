package com.eventra.backend.module.messaging.repository;

import com.eventra.backend.module.messaging.entity.CommunityMessage;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface CommunityMessageRepository extends JpaRepository<CommunityMessage, UUID> {
    List<CommunityMessage> findByCommunityIdOrderByCreatedAtAsc(Long communityId);
}

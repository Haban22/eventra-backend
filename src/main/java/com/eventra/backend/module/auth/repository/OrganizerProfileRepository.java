package com.eventra.backend.module.auth.repository;

import com.eventra.backend.module.auth.entity.OrganizerProfile;
import org.springframework.data.jpa.repository.JpaRepository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import java.util.Optional;
import java.util.UUID;

public interface OrganizerProfileRepository extends JpaRepository<OrganizerProfile, UUID> {
    Optional<OrganizerProfile> findByUserId(UUID userId);
    Page<OrganizerProfile> findByApprovedAtIsNullAndRejectionReasonIsNull(Pageable pageable);
}

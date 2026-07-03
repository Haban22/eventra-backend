package com.eventra.backend.module.auth.repository;

import com.eventra.backend.module.auth.entity.OtpCode;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;
import java.util.UUID;

public interface OtpCodeRepository extends JpaRepository<OtpCode, UUID> {
    Optional<OtpCode> findTopByUserIdAndUsedFalseOrderByCreatedAtDesc(UUID userId);
}

package com.eventra.backend.module.auth.repository;

import com.eventra.auth.entity.PasswordResetToken;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import java.util.Optional;
import java.util.UUID;

public interface PasswordResetTokenRepository extends JpaRepository<PasswordResetToken, UUID> {
    Optional<PasswordResetToken> findByTokenHash(String tokenHash);

    @Modifying
    @Query("update PasswordResetToken t set t.used = true where t.user.id = :userId and t.used = false")
    void markUnusedAsUsed(UUID userId);
}

package com.eventra.backend.module.auth.repository;

import com.eventra.backend.module.auth.entity.OtpCode;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;
import java.util.UUID;

public interface OtpCodeRepository extends JpaRepository<OtpCode, UUID> {

    Optional<OtpCode> findByCodeHash(String codeHash);

    @Modifying
    @Query("UPDATE OtpCode o SET o.used = true WHERE o.user.id = :userId AND o.purpose = :purpose AND o.used = false")
    void markAllUsed(@Param("userId") UUID userId, @Param("purpose") String purpose);
}

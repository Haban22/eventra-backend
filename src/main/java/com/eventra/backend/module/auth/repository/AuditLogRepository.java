package com.eventra.backend.module.auth.repository;

import com.eventra.backend.module.auth.entity.AdminAuditLog;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface AuditLogRepository extends JpaRepository<AdminAuditLog, UUID> {
    Page<AdminAuditLog> findAllByOrderByCreatedAtDesc(Pageable pageable);
    Page<AdminAuditLog> findByTargetTypeOrderByCreatedAtDesc(String targetType, Pageable pageable);
}

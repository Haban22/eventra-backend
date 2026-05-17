package com.eventra.backend.module.auth.repository;

import com.eventra.auth.entity.AdminAuditLog;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface AuditLogRepository extends JpaRepository<AdminAuditLog, UUID> {
}

package com.eventra.backend.module.auth.service;

import com.eventra.backend.module.auth.entity.AdminAuditLog;
import com.eventra.backend.module.auth.entity.UserStatus;
import com.eventra.backend.module.auth.repository.AuditLogRepository;
import org.springframework.stereotype.Service;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.UUID;

@Service
public class AuditService {
    private final AuditLogRepository auditLogRepository;

    public AuditService(AuditLogRepository auditLogRepository) {
        this.auditLogRepository = auditLogRepository;
    }

    // User-targeted actions (suspend/ban/approve organizer/etc.) — also stamps
    // targetType="user"/targetId=targetUserId so every row is queryable uniformly
    // through targetType/targetId regardless of which log() overload wrote it.
    public void log(UUID adminUserId, UUID targetUserId, String actionType, UserStatus previousStatus, UserStatus newStatus, String reason, String ipAddress) {
        AdminAuditLog log = new AdminAuditLog();
        log.setAdminUserId(adminUserId);
        log.setTargetUserId(targetUserId);
        log.setTargetType("user");
        log.setTargetId(targetUserId.toString());
        log.setActionType(actionType);
        log.setPreviousStatus(previousStatus);
        log.setNewStatus(newStatus);
        log.setActionReason(reason);
        log.setIpAddress(parseIp(ipAddress));
        auditLogRepository.save(log);
    }

    // Generic actions with no single user target (config changes, event approval,
    // payout decisions, content moderation) — no previous/new UserStatus to record.
    public void logGeneric(UUID adminUserId, String targetType, String targetId, String actionType, String reason, String ipAddress) {
        AdminAuditLog log = new AdminAuditLog();
        log.setAdminUserId(adminUserId);
        log.setTargetType(targetType);
        log.setTargetId(targetId);
        log.setActionType(actionType);
        log.setActionReason(reason);
        log.setIpAddress(parseIp(ipAddress));
        auditLogRepository.save(log);
    }

    private InetAddress parseIp(String ipAddress) {
        if (ipAddress == null || ipAddress.isBlank()) {
            return null;
        }
        try {
            return InetAddress.getByName(ipAddress);
        } catch (UnknownHostException e) {
            return null;
        }
    }
}

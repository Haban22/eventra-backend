package com.eventra.backend.module.auth.service;

import com.eventra.auth.entity.AdminAuditLog;
import com.eventra.auth.entity.UserStatus;
import com.eventra.auth.repository.AuditLogRepository;
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

    public void log(UUID adminUserId, UUID targetUserId, String actionType, UserStatus previousStatus, UserStatus newStatus, String reason, String ipAddress) {
        AdminAuditLog log = new AdminAuditLog();
        log.setAdminUserId(adminUserId);
        log.setTargetUserId(targetUserId);
        log.setActionType(actionType);
        log.setPreviousStatus(previousStatus);
        log.setNewStatus(newStatus);
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

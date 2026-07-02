package com.eventra.backend.module.auth.controller;

import com.eventra.backend.module.auth.entity.AdminAuditLog;
import com.eventra.backend.module.auth.repository.AuditLogRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/admin/audit-logs")
@PreAuthorize("hasRole('ADMIN')")
public class AdminAuditLogController {

    private final AuditLogRepository auditLogRepository;

    public AdminAuditLogController(AuditLogRepository auditLogRepository) {
        this.auditLogRepository = auditLogRepository;
    }

    @GetMapping
    public Map<String, Object> list(
            @RequestParam(required = false) String targetType,
            @RequestParam(defaultValue = "0")  int page,
            @RequestParam(defaultValue = "20") int size) {

        Page<AdminAuditLog> logs = auditLogRepository.findAll(PageRequest.of(page, size));

        List<Map<String, Object>> items = logs.getContent().stream().map(log -> {
            Map<String, Object> item = new LinkedHashMap<>();
            item.put("id",              log.getId());
            item.put("admin_user_id",   log.getAdminUserId());
            item.put("target_user_id",  log.getTargetUserId());
            item.put("action_type",     log.getActionType());
            item.put("previous_status", log.getPreviousStatus());
            item.put("new_status",      log.getNewStatus());
            item.put("action_reason",   log.getActionReason());
            item.put("ip_address",      log.getIpAddress());
            item.put("created_at",      log.getCreatedAt());
            return item;
        }).toList();

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("data",  items);
        result.put("total", logs.getTotalElements());
        result.put("page",  page);
        result.put("size",  size);
        return result;
    }
}

package com.eventra.backend.module.event.controller;

import com.eventra.backend.module.auth.security.AuthPrincipal;
import com.eventra.backend.module.event.dto.request.EventApprovalRequest;
import com.eventra.backend.module.event.dto.response.EventApprovalResponse;
import com.eventra.backend.module.event.service.EventApprovalService;
import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/events")
@PreAuthorize("hasRole('ADMIN')")
public class EventApprovalController {

    private final EventApprovalService approvalService;

    public EventApprovalController(EventApprovalService approvalService) {
        this.approvalService = approvalService;
    }

    @PostMapping("/{id}/approve")
    public EventApprovalResponse approveEvent(
            @AuthenticationPrincipal AuthPrincipal principal,
            @PathVariable UUID id,
            @Valid @RequestBody EventApprovalRequest request) {
        return approvalService.approveEvent(principal.userId(), id, request);
    }

    @GetMapping("/{id}/approval")
    public EventApprovalResponse getApproval(@PathVariable UUID id) {
        return approvalService.getApprovalForEvent(id);
    }
}
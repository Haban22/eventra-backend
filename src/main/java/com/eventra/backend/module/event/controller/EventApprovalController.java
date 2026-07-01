package com.eventra.backend.module.event.controller;

import com.eventra.backend.module.auth.security.AuthPrincipal;
import com.eventra.backend.module.event.dto.request.EventApprovalRequest;
import com.eventra.backend.module.event.dto.response.EventApprovalResponse;
import com.eventra.backend.module.event.dto.response.EventResponse;
import com.eventra.backend.module.event.enums.EventStatus;
import com.eventra.backend.module.event.service.EventApprovalService;
import com.eventra.backend.module.event.service.EventService;
import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/events")
@PreAuthorize("hasRole('ADMIN')")
public class EventApprovalController {

    private final EventApprovalService approvalService;
    private final EventService eventService;

    public EventApprovalController(EventApprovalService approvalService, EventService eventService) {
        this.approvalService = approvalService;
        this.eventService = eventService;
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

    /** Admin moderation queue — e.g. GET /api/v1/events/by-status?status=PENDING_APPROVAL */
    @GetMapping("/by-status")
    public List<EventResponse> getEventsByStatus(@RequestParam EventStatus status) {
        return eventService.getEventsByStatus(status);
    }
}
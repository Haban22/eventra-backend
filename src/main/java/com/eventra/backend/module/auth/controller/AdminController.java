package com.eventra.backend.module.auth.controller;

import com.eventra.auth.dto.request.AdminReasonRequest;
import com.eventra.auth.dto.response.*;
import com.eventra.auth.security.AuthPrincipal;
import com.eventra.auth.service.AdminService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/admin")
@PreAuthorize("hasRole('ADMIN')")
public class AdminController {
    private final AdminService adminService;

    public AdminController(AdminService adminService) {
        this.adminService = adminService;
    }

    @GetMapping("/organizers/pending")
    public PageResponse<OrganizerSummaryResponse> pending(@RequestParam(defaultValue = "0") int page, @RequestParam(defaultValue = "20") int size) {
        return adminService.pendingOrganizers(page, size);
    }

    @GetMapping("/organizers/{id}")
    public OrganizerDetailResponse organizer(@PathVariable UUID id) {
        return adminService.organizer(id);
    }

    @PatchMapping("/organizers/{id}/approve")
    public MessageResponse approve(@AuthenticationPrincipal AuthPrincipal principal, @PathVariable UUID id, HttpServletRequest request) {
        adminService.approveOrganizer(principal.userId(), id, clientIp(request));
        return new MessageResponse("Organizer approved");
    }

    @PatchMapping("/organizers/{id}/reject")
    public MessageResponse reject(@AuthenticationPrincipal AuthPrincipal principal, @PathVariable UUID id, @RequestBody(required = false) AdminReasonRequest body, HttpServletRequest request) {
        adminService.rejectOrganizer(principal.userId(), id, body == null ? null : body.reason(), clientIp(request));
        return new MessageResponse("Organizer rejected");
    }

    @PatchMapping("/users/{id}/suspend")
    public MessageResponse suspend(@AuthenticationPrincipal AuthPrincipal principal, @PathVariable UUID id, @RequestBody(required = false) AdminReasonRequest body, HttpServletRequest request) {
        adminService.suspend(principal.userId(), id, body == null ? null : body.reason(), clientIp(request));
        return new MessageResponse("User suspended");
    }

    @PatchMapping("/users/{id}/ban")
    public MessageResponse ban(@AuthenticationPrincipal AuthPrincipal principal, @PathVariable UUID id, @RequestBody(required = false) AdminReasonRequest body, HttpServletRequest request) {
        adminService.ban(principal.userId(), id, body == null ? null : body.reason(), clientIp(request));
        return new MessageResponse("User banned");
    }

    @PatchMapping("/users/{id}/reactivate")
    public MessageResponse reactivate(@AuthenticationPrincipal AuthPrincipal principal, @PathVariable UUID id, HttpServletRequest request) {
        adminService.reactivate(principal.userId(), id, clientIp(request));
        return new MessageResponse("User reactivated");
    }

    @PatchMapping("/users/{id}/disable")
    public MessageResponse disable(@AuthenticationPrincipal AuthPrincipal principal, @PathVariable UUID id, @RequestBody(required = false) AdminReasonRequest body, HttpServletRequest request) {
        adminService.disable(principal.userId(), id, body == null ? null : body.reason(), clientIp(request));
        return new MessageResponse("User disabled");
    }

    private String clientIp(HttpServletRequest request) {
        return request.getRemoteAddr();
    }
}

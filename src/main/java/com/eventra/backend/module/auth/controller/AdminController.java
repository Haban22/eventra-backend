package com.eventra.backend.module.auth.controller;

import com.eventra.backend.module.auth.dto.request.AdminReasonRequest;
import com.eventra.backend.module.auth.dto.request.SuspendUserRequest;
import com.eventra.backend.module.auth.dto.response.*;
import com.eventra.backend.module.auth.security.AuthPrincipal;
import com.eventra.backend.module.auth.service.AdminService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
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

    // ── User management ───────────────────────────────────────────────────────

    @GetMapping("/users")
    public PageResponse<UserSummaryResponse> listUsers(
            @RequestParam(required = false) String role,
            @RequestParam(required = false) String status,
            @RequestParam(defaultValue = "0")  int page,
            @RequestParam(defaultValue = "20") int size) {
        return adminService.listUsers(role, status, page, size);
    }

    @PatchMapping("/users/{id}/suspend")
    public MessageResponse suspend(@AuthenticationPrincipal AuthPrincipal principal,
                                   @PathVariable UUID id,
                                   @RequestBody(required = false) AdminReasonRequest body,
                                   HttpServletRequest request) {
        adminService.suspend(principal.userId(), id, body == null ? null : body.reason(), clientIp(request));
        return new MessageResponse("User suspended");
    }

    @PatchMapping("/users/{id}/suspend-detailed")
    public MessageResponse suspendDetailed(@AuthenticationPrincipal AuthPrincipal principal,
                                           @PathVariable UUID id,
                                           @Valid @RequestBody SuspendUserRequest body,
                                           HttpServletRequest request) {
        adminService.suspendWithDetails(principal.userId(), id, body.reason(), body.suspendedUntil(), clientIp(request));
        return new MessageResponse("User suspended");
    }

    @PatchMapping("/users/{id}/ban")
    public MessageResponse ban(@AuthenticationPrincipal AuthPrincipal principal,
                               @PathVariable UUID id,
                               @RequestBody(required = false) AdminReasonRequest body,
                               HttpServletRequest request) {
        adminService.ban(principal.userId(), id, body == null ? null : body.reason(), clientIp(request));
        return new MessageResponse("User banned");
    }

    @PatchMapping("/users/{id}/reactivate")
    public MessageResponse reactivate(@AuthenticationPrincipal AuthPrincipal principal,
                                      @PathVariable UUID id,
                                      HttpServletRequest request) {
        adminService.reactivate(principal.userId(), id, clientIp(request));
        return new MessageResponse("User reactivated");
    }

    @PatchMapping("/users/{id}/disable")
    public MessageResponse disable(@AuthenticationPrincipal AuthPrincipal principal,
                                   @PathVariable UUID id,
                                   @RequestBody(required = false) AdminReasonRequest body,
                                   HttpServletRequest request) {
        adminService.disable(principal.userId(), id, body == null ? null : body.reason(), clientIp(request));
        return new MessageResponse("User disabled");
    }

    @PatchMapping("/users/{id}/force-password-reset")
    public MessageResponse forcePasswordReset(@AuthenticationPrincipal AuthPrincipal principal,
                                              @PathVariable UUID id,
                                              HttpServletRequest request) {
        adminService.forcePasswordReset(principal.userId(), id, clientIp(request));
        return new MessageResponse("Password reset forced");
    }

    // ── Organizer management ──────────────────────────────────────────────────

    @GetMapping("/organizers/pending")
    public PageResponse<OrganizerSummaryResponse> pending(
            @RequestParam(defaultValue = "0")  int page,
            @RequestParam(defaultValue = "20") int size) {
        return adminService.pendingOrganizers(page, size);
    }

    @GetMapping("/organizers/{id}")
    public OrganizerDetailResponse organizer(@PathVariable UUID id) {
        return adminService.organizer(id);
    }

    @PatchMapping("/organizers/{id}/approve")
    public MessageResponse approve(@AuthenticationPrincipal AuthPrincipal principal,
                                   @PathVariable UUID id,
                                   HttpServletRequest request) {
        adminService.approveOrganizer(principal.userId(), id, clientIp(request));
        return new MessageResponse("Organizer approved");
    }

    @PatchMapping("/organizers/{id}/reject")
    public MessageResponse reject(@AuthenticationPrincipal AuthPrincipal principal,
                                  @PathVariable UUID id,
                                  @RequestBody(required = false) AdminReasonRequest body,
                                  HttpServletRequest request) {
        adminService.rejectOrganizer(principal.userId(), id, body == null ? null : body.reason(), clientIp(request));
        return new MessageResponse("Organizer rejected");
    }

    @PatchMapping("/organizers/{id}/verify")
    public MessageResponse verifyOrganizer(@AuthenticationPrincipal AuthPrincipal principal,
                                           @PathVariable UUID id,
                                           HttpServletRequest request) {
        adminService.verifyOrganizer(principal.userId(), id, clientIp(request));
        return new MessageResponse("Organizer verified");
    }

    private String clientIp(HttpServletRequest request) {
        return request.getRemoteAddr();
    }
}

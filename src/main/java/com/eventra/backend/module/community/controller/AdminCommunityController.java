package com.eventra.backend.module.community.controller;

import com.eventra.backend.common.response.ApiResponse;
import com.eventra.backend.module.community.dto.*;
import com.eventra.backend.module.community.service.ModerationService;
import com.eventra.backend.module.community.service.CommunityService;
import java.util.List;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.eventra.backend.module.auth.security.AuthPrincipal;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;

@RestController
@RequestMapping("/api/admin/community")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class AdminCommunityController {

    private final ModerationService moderationService;
    private final CommunityService communityService;

    @GetMapping("/flagged")
    public ResponseEntity<ApiResponse<ModerationStatsResponse>> getFlaggedContent() {
        return ResponseEntity.ok(ApiResponse.success(moderationService.getFlaggedContent()));
    }

    @PostMapping("/flag")
    public ResponseEntity<ApiResponse<FlaggedContentResponse>> flagContent(
            @Valid @RequestBody FlagContentRequest req) {
        return ResponseEntity.ok(ApiResponse.success(moderationService.flagContent(req)));
    }

    @PostMapping("/flagged/{id}/approve")
    public ResponseEntity<ApiResponse<FlaggedContentResponse>> approveContent(
            @AuthenticationPrincipal AuthPrincipal principal,
            @PathVariable Long id,
            HttpServletRequest request) {
        return ResponseEntity.ok(ApiResponse.success(moderationService.approveContent(id, principal.userId(), request.getRemoteAddr())));
    }

    @PostMapping("/flagged/{id}/remove")
    public ResponseEntity<ApiResponse<FlaggedContentResponse>> removeContent(
            @AuthenticationPrincipal AuthPrincipal principal,
            @PathVariable Long id,
            HttpServletRequest request) {
        return ResponseEntity.ok(ApiResponse.success(moderationService.removeContent(id, principal.userId(), request.getRemoteAddr())));
    }

    @PostMapping("/flagged/{id}/warn")
    public ResponseEntity<ApiResponse<FlaggedContentResponse>> warnUser(
            @AuthenticationPrincipal AuthPrincipal principal,
            @PathVariable Long id,
            HttpServletRequest request) {
        return ResponseEntity.ok(ApiResponse.success(moderationService.warnUser(id, principal.userId(), request.getRemoteAddr())));
    }

    @GetMapping("/pending-communities")
    public ResponseEntity<ApiResponse<List<CommunityResponse>>> getPendingCommunities() {
        return ResponseEntity.ok(ApiResponse.success(communityService.getPendingCommunities()));
    }

    @PostMapping("/communities/{id}/approve")
    public ResponseEntity<ApiResponse<CommunityResponse>> approveCommunity(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success(communityService.approveCommunity(id)));
    }

    @PostMapping("/communities/{id}/reject")
    public ResponseEntity<ApiResponse<Void>> rejectCommunity(@PathVariable Long id) {
        communityService.rejectCommunity(id);
        return ResponseEntity.ok(ApiResponse.success(null));
    }
}

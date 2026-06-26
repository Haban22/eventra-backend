package com.eventra.backend.module.community.controller;

import com.eventra.backend.common.response.ApiResponse;
import com.eventra.backend.module.community.dto.*;
import com.eventra.backend.module.community.service.ModerationService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/admin/community")
@RequiredArgsConstructor
public class AdminCommunityController {

    private final ModerationService moderationService;

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
            @PathVariable Long id,
            @RequestParam(defaultValue = "0") Long moderatorId) {
        return ResponseEntity.ok(ApiResponse.success(moderationService.approveContent(id, moderatorId)));
    }

    @PostMapping("/flagged/{id}/remove")
    public ResponseEntity<ApiResponse<FlaggedContentResponse>> removeContent(
            @PathVariable Long id,
            @RequestParam(defaultValue = "0") Long moderatorId) {
        return ResponseEntity.ok(ApiResponse.success(moderationService.removeContent(id, moderatorId)));
    }

    @PostMapping("/flagged/{id}/warn")
    public ResponseEntity<ApiResponse<FlaggedContentResponse>> warnUser(
            @PathVariable Long id,
            @RequestParam(defaultValue = "0") Long moderatorId) {
        return ResponseEntity.ok(ApiResponse.success(moderationService.warnUser(id, moderatorId)));
    }
}

package com.eventra.backend.module.community.controller;

import com.eventra.backend.common.response.ApiResponse;
import com.eventra.backend.module.community.dto.*;
import com.eventra.backend.module.community.service.DiscussionService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/communities/{communityId}/discussions")
@RequiredArgsConstructor
public class DiscussionController {

    private final DiscussionService discussionService;

    @GetMapping
    public ResponseEntity<ApiResponse<List<DiscussionResponse>>> getDiscussions(
            @PathVariable Long communityId) {
        return ResponseEntity.ok(ApiResponse.success(discussionService.getDiscussions(communityId)));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<DiscussionResponse>> createDiscussion(
            @PathVariable Long communityId,
            @Valid @RequestBody CreateDiscussionRequest req) {
        return ResponseEntity.ok(ApiResponse.success(discussionService.createDiscussion(communityId, req)));
    }

    @GetMapping("/{discussionId}")
    public ResponseEntity<ApiResponse<DiscussionResponse>> getDiscussion(
            @PathVariable Long communityId,
            @PathVariable Long discussionId) {
        return ResponseEntity.ok(ApiResponse.success(discussionService.getDiscussion(communityId, discussionId)));
    }

    @GetMapping("/{discussionId}/replies")
    public ResponseEntity<ApiResponse<List<DiscussionReplyResponse>>> getReplies(
            @PathVariable Long communityId,
            @PathVariable Long discussionId) {
        return ResponseEntity.ok(ApiResponse.success(discussionService.getReplies(communityId, discussionId)));
    }

    @PostMapping("/{discussionId}/replies")
    public ResponseEntity<ApiResponse<DiscussionReplyResponse>> addReply(
            @PathVariable Long communityId,
            @PathVariable Long discussionId,
            @Valid @RequestBody CreateDiscussionReplyRequest req) {
        return ResponseEntity.ok(ApiResponse.success(
                discussionService.addReply(communityId, discussionId, req)));
    }
}

package com.eventra.backend.module.community.controller;

import com.eventra.backend.common.response.ApiResponse;
import com.eventra.backend.module.community.dto.*;
import com.eventra.backend.module.community.service.CommunityService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/communities")
@RequiredArgsConstructor
public class CommunityController {

    private final CommunityService communityService;

    @GetMapping
    public ResponseEntity<ApiResponse<List<CommunityResponse>>> getCommunities(
            @RequestParam(required = false) String search,
            @RequestParam(required = false) String category,
            @RequestParam(defaultValue = "popular") String sort,
            @RequestParam(required = false) Long userId) {
        return ResponseEntity.ok(ApiResponse.success(
                communityService.getCommunities(search, category, sort, userId)));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<CommunityResponse>> getCommunity(
            @PathVariable Long id,
            @RequestParam(required = false) Long userId) {
        return ResponseEntity.ok(ApiResponse.success(communityService.getCommunity(id, userId)));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<CommunityResponse>> createCommunity(
            @Valid @RequestBody CreateCommunityRequest req) {
        return ResponseEntity.ok(ApiResponse.success(communityService.createCommunity(req)));
    }

    @PostMapping("/{id}/join")
    public ResponseEntity<ApiResponse<CommunityResponse>> joinCommunity(
            @PathVariable Long id,
            @Valid @RequestBody JoinCommunityRequest req) {
        return ResponseEntity.ok(ApiResponse.success(communityService.joinCommunity(id, req)));
    }

    @DeleteMapping("/{id}/leave")
    public ResponseEntity<ApiResponse<CommunityResponse>> leaveCommunity(
            @PathVariable Long id,
            @Valid @RequestBody LeaveCommunityRequest req) {
        return ResponseEntity.ok(ApiResponse.success(communityService.leaveCommunity(id, req.getUserId())));
    }

    @GetMapping("/{id}/members")
    public ResponseEntity<ApiResponse<List<CommunityMemberResponse>>> getMembers(
            @PathVariable Long id,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        return ResponseEntity.ok(ApiResponse.success(communityService.getMembers(id, page, size)));
    }
}

package com.eventra.backend.module.community.service;

import com.eventra.backend.common.exception.ResourceNotFoundException;
import com.eventra.backend.common.exception.ValidationException;
import com.eventra.backend.module.community.dto.*;
import com.eventra.backend.module.community.entity.Community;
import com.eventra.backend.module.community.entity.CommunityMember;
import com.eventra.backend.module.community.repository.CommunityMemberRepository;
import com.eventra.backend.module.community.repository.CommunityRepository;
import com.eventra.backend.module.gamification.dto.AwardActionRequest;
import com.eventra.backend.module.gamification.enums.GamificationAction;
import com.eventra.backend.module.gamification.service.RewardsService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class CommunityService {

    private final CommunityRepository communityRepository;
    private final CommunityMemberRepository memberRepository;
    private final RewardsService rewardsService;

    public List<CommunityResponse> getCommunities(String search, String category, String sort, UUID userId, UUID creatorId) {
        List<Community> communities;

        if (creatorId != null) {
            communities = communityRepository.findAllByCreatedByUserId(creatorId);
        } else if (search != null && !search.isBlank()) {
            communities = (category != null && !category.isBlank())
                    ? communityRepository.searchByNameOrDescriptionAndCategory(search.trim(), category)
                    : communityRepository.searchByNameOrDescription(search.trim());
        } else if (category != null && !category.isBlank()) {
            communities = communityRepository.findAllByCategoryAndActiveTrueOrderByMemberCountDesc(category);
        } else {
            communities = switch (sort != null ? sort.toLowerCase() : "popular") {
                case "events" -> communityRepository.findAllByActiveTrueOrderByEventCountDesc();
                case "az"     -> communityRepository.findAllByActiveTrueOrderByNameAsc();
                default       -> communityRepository.findAllByActiveTrueOrderByMemberCountDesc();
            };
        }

        Set<Long> joinedIds = userId != null
                ? memberRepository.findByUserId(userId).stream()
                    .map(CommunityMember::getCommunityId)
                    .collect(Collectors.toSet())
                : Set.of();

        return communities.stream()
                .map(c -> toResponse(c, joinedIds.contains(c.getId())))
                .collect(Collectors.toList());
    }

    public CommunityResponse getCommunity(Long communityId, UUID userId) {
        Community community = communityRepository.findByIdAndActiveTrue(communityId)
                .orElseThrow(() -> new ResourceNotFoundException("Community not found: " + communityId));
        boolean joined = userId != null && memberRepository.existsByCommunityIdAndUserId(communityId, userId);
        return toResponse(community, joined);
    }

    @Transactional
    public CommunityResponse createCommunity(CreateCommunityRequest req) {
        if (communityRepository.existsByNameIgnoreCase(req.getName())) {
            throw new ValidationException("A community with this name already exists");
        }
        Community c = new Community();
        c.setName(req.getName());
        c.setDescription(req.getDescription());
        c.setCoverImage(req.getCoverImage());
        c.setCategory(req.getCategory());
        c.setCreatedByUserId(req.getCreatedByUserId());
        c.setActive(false);
        c = communityRepository.save(c);
        return toResponse(c, false);
    }

    @Transactional
    public CommunityResponse joinCommunity(Long communityId, JoinCommunityRequest req) {
        Community community = communityRepository.findByIdAndActiveTrue(communityId)
                .orElseThrow(() -> new ResourceNotFoundException("Community not found: " + communityId));

        if (memberRepository.existsByCommunityIdAndUserId(communityId, req.getUserId())) {
            return toResponse(community, true);
        }

        CommunityMember member = new CommunityMember();
        member.setCommunityId(communityId);
        member.setUserId(req.getUserId());
        member.setDisplayName(req.getDisplayName() != null ? req.getDisplayName() : "User " + req.getUserId().toString());
        member.setAvatarUrl(req.getAvatarUrl());
        memberRepository.save(member);

        community.setMemberCount(community.getMemberCount() + 1);
        community = communityRepository.save(community);

        // Award XP for joining community (same action as discussion join)
        try {
            AwardActionRequest award = new AwardActionRequest();
            award.setUserId(req.getUserId());
            award.setAction(GamificationAction.JOIN_DISCUSSION);
            award.setDisplayName(req.getDisplayName());
            award.setAvatarUrl(req.getAvatarUrl());
            award.setReferenceId("community-join-" + communityId);
            rewardsService.awardAction(award);
        } catch (Exception e) {
            log.warn("Failed to award XP for community join. User: {}, Community: {}. Reason: {}", 
                req.getUserId(), communityId, e.getMessage(), e);
        }

        return toResponse(community, true);
    }

    @Transactional
    public CommunityResponse leaveCommunity(Long communityId, UUID userId) {
        Community community = communityRepository.findByIdAndActiveTrue(communityId)
                .orElseThrow(() -> new ResourceNotFoundException("Community not found: " + communityId));

        CommunityMember member = memberRepository.findByCommunityIdAndUserId(communityId, userId)
                .orElseThrow(() -> new ValidationException("User is not a member of this community"));

        memberRepository.delete(member);
        community.setMemberCount(Math.max(0L, community.getMemberCount() - 1));
        community = communityRepository.save(community);
        return toResponse(community, false);
    }

    public List<CommunityMemberResponse> getMembers(Long communityId, int page, int size) {
        communityRepository.findByIdAndActiveTrue(communityId)
                .orElseThrow(() -> new ResourceNotFoundException("Community not found: " + communityId));
        return memberRepository.findByCommunityIdOrderByJoinedAtAsc(communityId, PageRequest.of(page, size))
                .stream()
                .map(this::toMemberResponse)
                .collect(Collectors.toList());
    }

    public List<CommunityResponse> getPendingCommunities() {
        return communityRepository.findAllByActiveFalseOrderByCreatedAtDesc().stream()
                .map(c -> toResponse(c, false))
                .collect(Collectors.toList());
    }

    @Transactional
    public CommunityResponse approveCommunity(Long id) {
        Community c = communityRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Community not found: " + id));
        c.setActive(true);
        return toResponse(communityRepository.save(c), false);
    }

    @Transactional
    public void rejectCommunity(Long id) {
        Community c = communityRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Community not found: " + id));
        communityRepository.delete(c);
    }

    // ─── Helpers ─────────────────────────────────────────────────────────────

    CommunityResponse toResponse(Community c, boolean joined) {
        CommunityResponse r = new CommunityResponse();
        r.setId(c.getId());
        r.setName(c.getName());
        r.setDescription(c.getDescription());
        r.setCoverImage(c.getCoverImage());
        r.setCategory(c.getCategory());
        r.setMemberCount(c.getMemberCount());
        r.setEventCount(c.getEventCount());
        r.setJoined(joined);
        r.setActive(c.getActive() != null ? c.getActive() : false);
        r.setCreatedAt(c.getCreatedAt());
        r.setCreatedByUserId(c.getCreatedByUserId() != null ? c.getCreatedByUserId().toString() : null);
        return r;
    }

    private CommunityMemberResponse toMemberResponse(CommunityMember m) {
        CommunityMemberResponse r = new CommunityMemberResponse();
        r.setUserId(m.getUserId());
        r.setDisplayName(m.getDisplayName());
        r.setAvatarUrl(m.getAvatarUrl());
        r.setRole(m.getRole());
        r.setJoinedAt(m.getJoinedAt());
        return r;
    }
}

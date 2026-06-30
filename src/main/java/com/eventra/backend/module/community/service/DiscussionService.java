package com.eventra.backend.module.community.service;

import com.eventra.backend.common.exception.ResourceNotFoundException;
import com.eventra.backend.module.community.dto.*;
import com.eventra.backend.module.community.entity.Discussion;
import com.eventra.backend.module.community.entity.DiscussionReply;
import com.eventra.backend.module.community.repository.CommunityRepository;
import com.eventra.backend.module.community.repository.DiscussionReplyRepository;
import com.eventra.backend.module.community.repository.DiscussionRepository;
import com.eventra.backend.module.gamification.dto.AwardActionRequest;
import com.eventra.backend.module.gamification.enums.GamificationAction;
import com.eventra.backend.module.gamification.service.RewardsService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class DiscussionService {

    private static final int HOT_THRESHOLD = 5;

    private final DiscussionRepository discussionRepository;
    private final DiscussionReplyRepository replyRepository;
    private final CommunityRepository communityRepository;
    private final RewardsService rewardsService;

    public List<DiscussionResponse> getDiscussions(Long communityId) {
        communityRepository.findByIdAndActiveTrue(communityId)
                .orElseThrow(() -> new ResourceNotFoundException("Community not found: " + communityId));
        return discussionRepository
                .findByCommunityIdAndActiveTrueOrderByHotDescCreatedAtDesc(communityId)
                .stream()
                .map(this::toDiscussionResponse)
                .collect(Collectors.toList());
    }

    @Transactional
    public DiscussionResponse createDiscussion(Long communityId, CreateDiscussionRequest req) {
        communityRepository.findByIdAndActiveTrue(communityId)
                .orElseThrow(() -> new ResourceNotFoundException("Community not found: " + communityId));

        Discussion d = new Discussion();
        d.setCommunityId(communityId);
        d.setTitle(req.getTitle());
        d.setContent(req.getContent());
        d.setAuthorId(req.getAuthorId());
        d.setAuthorName(req.getAuthorName());
        d.setAuthorAvatar(req.getAuthorAvatar());
        d = discussionRepository.save(d);
        return toDiscussionResponse(d);
    }

    public DiscussionResponse getDiscussion(Long communityId, Long discussionId) {
        Discussion d = discussionRepository.findByIdAndCommunityIdAndActiveTrue(discussionId, communityId)
                .orElseThrow(() -> new ResourceNotFoundException("Discussion not found: " + discussionId));
        return toDiscussionResponse(d);
    }

    public List<DiscussionReplyResponse> getReplies(Long communityId, Long discussionId) {
        discussionRepository.findByIdAndCommunityIdAndActiveTrue(discussionId, communityId)
                .orElseThrow(() -> new ResourceNotFoundException("Discussion not found: " + discussionId));
        return replyRepository.findByDiscussionIdAndActiveTrueOrderByCreatedAtAsc(discussionId)
                .stream()
                .map(this::toReplyResponse)
                .collect(Collectors.toList());
    }

    @Transactional
    public DiscussionReplyResponse addReply(Long communityId, Long discussionId,
                                            CreateDiscussionReplyRequest req) {
        Discussion discussion = discussionRepository.findByIdAndCommunityIdAndActiveTrue(discussionId, communityId)
                .orElseThrow(() -> new ResourceNotFoundException("Discussion not found: " + discussionId));

        DiscussionReply reply = new DiscussionReply();
        reply.setDiscussionId(discussionId);
        reply.setAuthorId(req.getAuthorId());
        reply.setAuthorName(req.getAuthorName());
        reply.setAuthorAvatar(req.getAuthorAvatar());
        reply.setContent(req.getContent());
        reply = replyRepository.save(reply);

        // Increment reply count and check hot threshold
        discussion.setReplyCount(discussion.getReplyCount() + 1);
        if (!discussion.getHot() && discussion.getReplyCount() >= HOT_THRESHOLD) {
            discussion.setHot(true);
        }
        discussionRepository.save(discussion);

        // Award XP for joining a discussion
        try {
            AwardActionRequest award = new AwardActionRequest();
            award.setUserId(req.getAuthorId());
            award.setAction(GamificationAction.JOIN_DISCUSSION);
            award.setDisplayName(req.getAuthorName());
            award.setAvatarUrl(req.getAuthorAvatar());
            award.setReferenceId("discussion-reply-" + reply.getId());
            rewardsService.awardAction(award);
        } catch (Exception e) {
            log.warn("Failed to award XP for discussion reply. User: {}, Reply: {}. Reason: {}", 
                req.getAuthorId(), reply.getId(), e.getMessage(), e);
        }

        return toReplyResponse(reply);
    }

    // ─── Helpers ─────────────────────────────────────────────────────────────

    DiscussionResponse toDiscussionResponse(Discussion d) {
        DiscussionResponse r = new DiscussionResponse();
        r.setId(d.getId());
        r.setCommunityId(d.getCommunityId());
        r.setTitle(d.getTitle());
        r.setContent(d.getContent());
        r.setAuthorId(d.getAuthorId());
        r.setAuthorName(d.getAuthorName());
        r.setAuthorAvatar(d.getAuthorAvatar());
        r.setReplyCount(d.getReplyCount());
        r.setHot(d.getHot());
        r.setCreatedAt(d.getCreatedAt());
        r.setUpdatedAt(d.getUpdatedAt());
        return r;
    }

    DiscussionReplyResponse toReplyResponse(DiscussionReply reply) {
        DiscussionReplyResponse r = new DiscussionReplyResponse();
        r.setId(reply.getId());
        r.setDiscussionId(reply.getDiscussionId());
        r.setAuthorId(reply.getAuthorId());
        r.setAuthorName(reply.getAuthorName());
        r.setAuthorAvatar(reply.getAuthorAvatar());
        r.setContent(reply.getContent());
        r.setCreatedAt(reply.getCreatedAt());
        return r;
    }
}

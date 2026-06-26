package com.eventra.backend.module.community.service;

import com.eventra.backend.common.exception.ResourceNotFoundException;
import com.eventra.backend.module.community.dto.FlagContentRequest;
import com.eventra.backend.module.community.dto.FlaggedContentResponse;
import com.eventra.backend.module.community.dto.ModerationStatsResponse;
import com.eventra.backend.module.community.entity.Discussion;
import com.eventra.backend.module.community.entity.DiscussionReply;
import com.eventra.backend.module.community.entity.FlaggedContent;
import com.eventra.backend.module.community.enums.ContentType;
import com.eventra.backend.module.community.enums.FlagStatus;
import com.eventra.backend.module.community.repository.DiscussionReplyRepository;
import com.eventra.backend.module.community.repository.DiscussionRepository;
import com.eventra.backend.module.community.repository.FlaggedContentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ModerationService {

    private final FlaggedContentRepository flaggedContentRepository;
    private final DiscussionRepository discussionRepository;
    private final DiscussionReplyRepository replyRepository;

    public ModerationStatsResponse getFlaggedContent() {
        List<FlaggedContentResponse> items = flaggedContentRepository.findAllByOrderByCreatedAtDesc()
                .stream()
                .map(this::toResponse)
                .collect(Collectors.toList());

        long pending = flaggedContentRepository.countByStatus(FlagStatus.PENDING);
        long total = flaggedContentRepository.count();
        long resolvedToday = flaggedContentRepository.findAllByOrderByCreatedAtDesc().stream()
                .filter(f -> f.getStatus() != FlagStatus.PENDING
                        && f.getResolvedAt() != null
                        && f.getResolvedAt().toLocalDate().equals(LocalDateTime.now().toLocalDate()))
                .count();

        return new ModerationStatsResponse(pending, resolvedToday, total, items);
    }

    @Transactional
    public FlaggedContentResponse flagContent(FlagContentRequest req) {
        // Increment if already flagged, else create new
        FlaggedContent flagged = flaggedContentRepository
                .findByContentTypeAndContentId(req.getContentType(), req.getContentId())
                .map(f -> { f.setReportCount(f.getReportCount() + 1); return f; })
                .orElseGet(() -> {
                    FlaggedContent f = new FlaggedContent();
                    f.setContentType(req.getContentType());
                    f.setContentId(req.getContentId());
                    f.setCommunityId(req.getCommunityId());
                    f.setAuthorId(req.getAuthorId());
                    f.setAuthorName(req.getAuthorName());
                    f.setContentPreview(req.getContentPreview());
                    f.setReason(req.getReason());
                    return f;
                });
        return toResponse(flaggedContentRepository.save(flagged));
    }

    @Transactional
    public FlaggedContentResponse approveContent(Long flagId, Long moderatorId) {
        FlaggedContent f = findFlag(flagId);
        f.setStatus(FlagStatus.APPROVED);
        f.setResolvedByUserId(moderatorId);
        f.setResolvedAt(LocalDateTime.now());
        return toResponse(flaggedContentRepository.save(f));
    }

    @Transactional
    public FlaggedContentResponse removeContent(Long flagId, Long moderatorId) {
        FlaggedContent f = findFlag(flagId);
        f.setStatus(FlagStatus.REMOVED);
        f.setResolvedByUserId(moderatorId);
        f.setResolvedAt(LocalDateTime.now());
        flaggedContentRepository.save(f);

        // Soft-delete the underlying content
        if (f.getContentType() == ContentType.DISCUSSION) {
            discussionRepository.findById(f.getContentId())
                    .ifPresent(d -> { d.setActive(false); discussionRepository.save(d); });
        } else if (f.getContentType() == ContentType.REPLY) {
            replyRepository.findById(f.getContentId())
                    .ifPresent(r -> { r.setActive(false); replyRepository.save(r); });
        }

        return toResponse(f);
    }

    @Transactional
    public FlaggedContentResponse warnUser(Long flagId, Long moderatorId) {
        FlaggedContent f = findFlag(flagId);
        f.setStatus(FlagStatus.WARNED);
        f.setResolvedByUserId(moderatorId);
        f.setResolvedAt(LocalDateTime.now());
        return toResponse(flaggedContentRepository.save(f));
    }

    // ─── Helpers ─────────────────────────────────────────────────────────────

    private FlaggedContent findFlag(Long id) {
        return flaggedContentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Flagged content not found: " + id));
    }

    FlaggedContentResponse toResponse(FlaggedContent f) {
        FlaggedContentResponse r = new FlaggedContentResponse();
        r.setId(f.getId());
        r.setContentType(f.getContentType());
        r.setContentId(f.getContentId());
        r.setCommunityId(f.getCommunityId());
        r.setAuthorId(f.getAuthorId());
        r.setAuthorName(f.getAuthorName());
        r.setContentPreview(f.getContentPreview());
        r.setReason(f.getReason());
        r.setReportCount(f.getReportCount());
        r.setStatus(f.getStatus());
        r.setCreatedAt(f.getCreatedAt());
        r.setResolvedAt(f.getResolvedAt());
        return r;
    }
}

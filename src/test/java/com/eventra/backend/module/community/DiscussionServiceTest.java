package com.eventra.backend.module.community;

import com.eventra.backend.common.exception.ResourceNotFoundException;
import com.eventra.backend.module.community.dto.*;
import com.eventra.backend.module.community.entity.Discussion;
import com.eventra.backend.module.community.entity.DiscussionReply;
import com.eventra.backend.module.community.repository.CommunityRepository;
import com.eventra.backend.module.community.repository.DiscussionReplyRepository;
import com.eventra.backend.module.community.repository.DiscussionRepository;
import com.eventra.backend.module.community.service.DiscussionService;
import com.eventra.backend.module.gamification.service.RewardsService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class DiscussionServiceTest {

    @Mock private DiscussionRepository discussionRepository;
    @Mock private DiscussionReplyRepository replyRepository;
    @Mock private CommunityRepository communityRepository;
    @Mock private RewardsService rewardsService;
    @InjectMocks private DiscussionService discussionService;

    private com.eventra.backend.module.community.entity.Community community;
    private Discussion discussion;

    @BeforeEach
    void setUp() {
        community = new com.eventra.backend.module.community.entity.Community();
        community.setId(1L);
        community.setName("Cairo Music Lovers");
        community.setCategory("Music");
        community.setMemberCount(100L);
        community.setEventCount(10L);
        community.setActive(true);

        discussion = new Discussion();
        discussion.setId(10L);
        discussion.setCommunityId(1L);
        discussion.setTitle("Best jazz venues?");
        discussion.setContent("Looking for recommendations");
        discussion.setAuthorId(5L);
        discussion.setAuthorName("Alice");
        discussion.setReplyCount(0);
        discussion.setHot(false);
        discussion.setActive(true);
        discussion.setCreatedAt(LocalDateTime.now());

        when(communityRepository.findByIdAndActiveTrue(1L)).thenReturn(Optional.of(community));
        when(discussionRepository.save(any(Discussion.class))).thenAnswer(inv -> inv.getArgument(0));
        when(replyRepository.save(any(DiscussionReply.class))).thenAnswer(inv -> inv.getArgument(0));
    }

    // ─── getDiscussions ───────────────────────────────────────────────────────

    @Test
    void getDiscussions_returnsListForCommunity() {
        when(discussionRepository.findByCommunityIdAndActiveTrueOrderByHotDescCreatedAtDesc(1L))
                .thenReturn(List.of(discussion));

        List<DiscussionResponse> result = discussionService.getDiscussions(1L);

        assertEquals(1, result.size());
        assertEquals("Best jazz venues?", result.get(0).getTitle());
        assertFalse(result.get(0).isHot());
    }

    @Test
    void getDiscussions_throwsWhenCommunityNotFound() {
        when(communityRepository.findByIdAndActiveTrue(99L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> discussionService.getDiscussions(99L));
    }

    // ─── createDiscussion ─────────────────────────────────────────────────────

    @Test
    void createDiscussion_savesDiscussion() {
        CreateDiscussionRequest req = new CreateDiscussionRequest();
        req.setAuthorId(5L);
        req.setAuthorName("Alice");
        req.setTitle("Best jazz venues?");
        req.setContent("Looking for recommendations");

        DiscussionResponse result = discussionService.createDiscussion(1L, req);

        assertNotNull(result);
        assertEquals("Best jazz venues?", result.getTitle());
        assertEquals(5L, result.getAuthorId());
        verify(discussionRepository).save(any(Discussion.class));
    }

    @Test
    void createDiscussion_throwsWhenCommunityNotFound() {
        when(communityRepository.findByIdAndActiveTrue(99L)).thenReturn(Optional.empty());

        CreateDiscussionRequest req = new CreateDiscussionRequest();
        req.setAuthorId(1L);
        req.setAuthorName("Bob");
        req.setTitle("Test");

        assertThrows(ResourceNotFoundException.class, () -> discussionService.createDiscussion(99L, req));
    }

    // ─── addReply ─────────────────────────────────────────────────────────────

    @Test
    void addReply_incrementsReplyCountAndAwardsXp() {
        when(discussionRepository.findByIdAndCommunityIdAndActiveTrue(10L, 1L))
                .thenReturn(Optional.of(discussion));

        CreateDiscussionReplyRequest req = new CreateDiscussionReplyRequest();
        req.setAuthorId(7L);
        req.setAuthorName("Bob");
        req.setContent("Great question!");

        DiscussionReplyResponse result = discussionService.addReply(1L, 10L, req);

        assertNotNull(result);
        assertEquals("Great question!", result.getContent());
        assertEquals(7L, result.getAuthorId());
        assertEquals(1, discussion.getReplyCount()); // incremented
        verify(discussionRepository).save(discussion);
        verify(rewardsService).awardAction(any());
    }

    @Test
    void addReply_marksDiscussionHotAfterFiveReplies() {
        discussion.setReplyCount(4); // one more will hit HOT_THRESHOLD = 5
        when(discussionRepository.findByIdAndCommunityIdAndActiveTrue(10L, 1L))
                .thenReturn(Optional.of(discussion));

        CreateDiscussionReplyRequest req = new CreateDiscussionReplyRequest();
        req.setAuthorId(7L);
        req.setAuthorName("Bob");
        req.setContent("Fifth reply!");

        discussionService.addReply(1L, 10L, req);

        assertTrue(discussion.getHot());
    }

    @Test
    void addReply_throwsWhenDiscussionNotFound() {
        when(discussionRepository.findByIdAndCommunityIdAndActiveTrue(99L, 1L))
                .thenReturn(Optional.empty());

        CreateDiscussionReplyRequest req = new CreateDiscussionReplyRequest();
        req.setAuthorId(1L);
        req.setAuthorName("Bob");
        req.setContent("Reply");

        assertThrows(ResourceNotFoundException.class, () -> discussionService.addReply(1L, 99L, req));
    }

    // ─── getReplies ───────────────────────────────────────────────────────────

    @Test
    void getReplies_returnsRepliesInOrder() {
        DiscussionReply r1 = new DiscussionReply();
        r1.setId(1L);
        r1.setDiscussionId(10L);
        r1.setAuthorName("Alice");
        r1.setContent("Reply 1");
        r1.setCreatedAt(LocalDateTime.now());

        when(discussionRepository.findByIdAndCommunityIdAndActiveTrue(10L, 1L))
                .thenReturn(Optional.of(discussion));
        when(replyRepository.findByDiscussionIdAndActiveTrueOrderByCreatedAtAsc(10L))
                .thenReturn(List.of(r1));

        List<DiscussionReplyResponse> result = discussionService.getReplies(1L, 10L);

        assertEquals(1, result.size());
        assertEquals("Reply 1", result.get(0).getContent());
    }

    // ─── getDiscussion ────────────────────────────────────────────────────────

    @Test
    void getDiscussion_returnsCorrectDiscussion() {
        when(discussionRepository.findByIdAndCommunityIdAndActiveTrue(10L, 1L))
                .thenReturn(Optional.of(discussion));

        DiscussionResponse result = discussionService.getDiscussion(1L, 10L);

        assertEquals(10L, result.getId());
        assertEquals("Best jazz venues?", result.getTitle());
    }

    @Test
    void getDiscussion_throwsWhenNotFound() {
        when(discussionRepository.findByIdAndCommunityIdAndActiveTrue(99L, 1L))
                .thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> discussionService.getDiscussion(1L, 99L));
    }
}

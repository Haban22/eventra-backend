package com.eventra.backend.module.community;

import com.eventra.backend.common.exception.ResourceNotFoundException;
import com.eventra.backend.common.exception.ValidationException;
import com.eventra.backend.module.community.dto.*;
import com.eventra.backend.module.community.entity.Community;
import com.eventra.backend.module.community.entity.CommunityMember;
import com.eventra.backend.module.community.repository.CommunityMemberRepository;
import com.eventra.backend.module.community.repository.CommunityRepository;
import com.eventra.backend.module.community.service.CommunityService;
import com.eventra.backend.module.gamification.service.RewardsService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.data.domain.Pageable;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class CommunityServiceTest {

    @Mock private CommunityRepository communityRepository;
    @Mock private CommunityMemberRepository memberRepository;
    @Mock private RewardsService rewardsService;
    @InjectMocks private CommunityService communityService;

    private Community community;

    @BeforeEach
    void setUp() {
        community = new Community();
        community.setId(1L);
        community.setName("Cairo Music Lovers");
        community.setDescription("A music community");
        community.setCategory("Music");
        community.setMemberCount(100L);
        community.setEventCount(10L);
        community.setActive(true);
        community.setCreatedAt(LocalDateTime.now());

        when(communityRepository.save(any(Community.class))).thenAnswer(inv -> inv.getArgument(0));
        when(memberRepository.save(any(CommunityMember.class))).thenAnswer(inv -> inv.getArgument(0));
    }

    // ─── getCommunities ───────────────────────────────────────────────────────

    @Test
    void getCommunities_returnsAllCommunitiesWithJoinStatus() {
        CommunityMember membership = new CommunityMember();
        membership.setCommunityId(1L);
        membership.setUserId(42L);

        when(communityRepository.findAllByActiveTrueOrderByMemberCountDesc()).thenReturn(List.of(community));
        when(memberRepository.findByUserId(42L)).thenReturn(List.of(membership));

        List<CommunityResponse> result = communityService.getCommunities(null, null, "popular", 42L);

        assertEquals(1, result.size());
        assertTrue(result.get(0).isJoined());
        assertEquals("Cairo Music Lovers", result.get(0).getName());
    }

    @Test
    void getCommunities_returnsNotJoinedWhenUserNotMember() {
        when(communityRepository.findAllByActiveTrueOrderByMemberCountDesc()).thenReturn(List.of(community));
        when(memberRepository.findByUserId(99L)).thenReturn(Collections.emptyList());

        List<CommunityResponse> result = communityService.getCommunities(null, null, "popular", 99L);

        assertEquals(1, result.size());
        assertFalse(result.get(0).isJoined());
    }

    @Test
    void getCommunities_sortsByEventCount() {
        when(communityRepository.findAllByActiveTrueOrderByEventCountDesc()).thenReturn(List.of(community));
        when(memberRepository.findByUserId(any())).thenReturn(Collections.emptyList());

        communityService.getCommunities(null, null, "events", null);

        verify(communityRepository).findAllByActiveTrueOrderByEventCountDesc();
    }

    @Test
    void getCommunities_sortsByNameAlphabetically() {
        when(communityRepository.findAllByActiveTrueOrderByNameAsc()).thenReturn(List.of(community));
        when(memberRepository.findByUserId(any())).thenReturn(Collections.emptyList());

        communityService.getCommunities(null, null, "az", null);

        verify(communityRepository).findAllByActiveTrueOrderByNameAsc();
    }

    @Test
    void getCommunities_filtersbyCategory() {
        when(communityRepository.findAllByCategoryAndActiveTrueOrderByMemberCountDesc("Music"))
                .thenReturn(List.of(community));
        when(memberRepository.findByUserId(any())).thenReturn(Collections.emptyList());

        List<CommunityResponse> result = communityService.getCommunities(null, "Music", "popular", null);

        assertEquals(1, result.size());
        assertEquals("Music", result.get(0).getCategory());
    }

    @Test
    void getCommunities_searchesByNameOrDescription() {
        when(communityRepository.searchByNameOrDescription("music")).thenReturn(List.of(community));
        when(memberRepository.findByUserId(any())).thenReturn(Collections.emptyList());

        List<CommunityResponse> result = communityService.getCommunities("music", null, "popular", null);

        assertEquals(1, result.size());
    }

    // ─── getCommunity ─────────────────────────────────────────────────────────

    @Test
    void getCommunity_returnsCorrectCommunity() {
        when(communityRepository.findByIdAndActiveTrue(1L)).thenReturn(Optional.of(community));
        when(memberRepository.existsByCommunityIdAndUserId(1L, 42L)).thenReturn(true);

        CommunityResponse result = communityService.getCommunity(1L, 42L);

        assertEquals(1L, result.getId());
        assertTrue(result.isJoined());
    }

    @Test
    void getCommunity_throwsWhenNotFound() {
        when(communityRepository.findByIdAndActiveTrue(99L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> communityService.getCommunity(99L, null));
    }

    // ─── createCommunity ─────────────────────────────────────────────────────

    @Test
    void createCommunity_savesAndReturnsCommunity() {
        when(communityRepository.existsByNameIgnoreCase("New Community")).thenReturn(false);

        CreateCommunityRequest req = new CreateCommunityRequest();
        req.setName("New Community");
        req.setCategory("Tech");

        CommunityResponse result = communityService.createCommunity(req);

        assertEquals("New Community", result.getName());
        verify(communityRepository).save(any(Community.class));
    }

    @Test
    void createCommunity_throwsOnDuplicateName() {
        when(communityRepository.existsByNameIgnoreCase("Cairo Music Lovers")).thenReturn(true);

        CreateCommunityRequest req = new CreateCommunityRequest();
        req.setName("Cairo Music Lovers");
        req.setCategory("Music");

        assertThrows(ValidationException.class, () -> communityService.createCommunity(req));
    }

    // ─── joinCommunity ────────────────────────────────────────────────────────

    @Test
    void joinCommunity_addsMemberAndIncrementCount() {
        when(communityRepository.findByIdAndActiveTrue(1L)).thenReturn(Optional.of(community));
        when(memberRepository.existsByCommunityIdAndUserId(1L, 5L)).thenReturn(false);

        JoinCommunityRequest req = new JoinCommunityRequest();
        req.setUserId(5L);
        req.setDisplayName("Bob");

        CommunityResponse result = communityService.joinCommunity(1L, req);

        assertTrue(result.isJoined());
        assertEquals(101L, result.getMemberCount());
        verify(memberRepository).save(any(CommunityMember.class));
    }

    @Test
    void joinCommunity_isIdempotentIfAlreadyMember() {
        when(communityRepository.findByIdAndActiveTrue(1L)).thenReturn(Optional.of(community));
        when(memberRepository.existsByCommunityIdAndUserId(1L, 5L)).thenReturn(true);

        JoinCommunityRequest req = new JoinCommunityRequest();
        req.setUserId(5L);

        CommunityResponse result = communityService.joinCommunity(1L, req);

        assertTrue(result.isJoined());
        verify(memberRepository, never()).save(any());
    }

    // ─── leaveCommunity ──────────────────────────────────────────────────────

    @Test
    void leaveCommunity_removesMemberAndDecrementsCount() {
        CommunityMember member = new CommunityMember();
        member.setUserId(5L);
        member.setCommunityId(1L);

        when(communityRepository.findByIdAndActiveTrue(1L)).thenReturn(Optional.of(community));
        when(memberRepository.findByCommunityIdAndUserId(1L, 5L)).thenReturn(Optional.of(member));

        CommunityResponse result = communityService.leaveCommunity(1L, 5L);

        assertFalse(result.isJoined());
        assertEquals(99L, result.getMemberCount());
        verify(memberRepository).delete(member);
    }

    @Test
    void leaveCommunity_throwsWhenNotMember() {
        when(communityRepository.findByIdAndActiveTrue(1L)).thenReturn(Optional.of(community));
        when(memberRepository.findByCommunityIdAndUserId(1L, 99L)).thenReturn(Optional.empty());

        assertThrows(ValidationException.class, () -> communityService.leaveCommunity(1L, 99L));
    }

    // ─── getMembers ───────────────────────────────────────────────────────────

    @Test
    void getMembers_returnsPagedMembers() {
        CommunityMember m = new CommunityMember();
        m.setUserId(5L);
        m.setDisplayName("Bob");
        m.setCommunityId(1L);

        when(communityRepository.findByIdAndActiveTrue(1L)).thenReturn(Optional.of(community));
        when(memberRepository.findByCommunityIdOrderByJoinedAtAsc(eq(1L), any(Pageable.class)))
                .thenReturn(List.of(m));

        List<CommunityMemberResponse> result = communityService.getMembers(1L, 0, 20);

        assertEquals(1, result.size());
        assertEquals(5L, result.get(0).getUserId());
        assertEquals("Bob", result.get(0).getDisplayName());
    }
}

package com.eventra.backend.module.community;

import com.eventra.backend.module.auth.entity.UserRole;
import com.eventra.backend.module.auth.security.AuthPrincipal;
import com.eventra.backend.module.auth.security.JwtUtil;
import com.eventra.backend.module.community.controller.AdminCommunityController;
import com.eventra.backend.module.community.controller.CommunityController;
import com.eventra.backend.module.community.controller.DiscussionController;
import com.eventra.backend.module.community.dto.*;
import com.eventra.backend.module.community.enums.ContentType;
import com.eventra.backend.module.community.enums.FlagStatus;
import com.eventra.backend.module.community.service.CommunityService;
import com.eventra.backend.module.community.service.DiscussionService;
import com.eventra.backend.module.community.service.ModerationService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.authentication;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest({CommunityController.class, DiscussionController.class, AdminCommunityController.class})
@Import(CommunityControllerTest.TestSecurityConfig.class)
class CommunityControllerTest {

    @TestConfiguration
    @EnableMethodSecurity
    static class TestSecurityConfig {
        @Bean
        SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
            http.csrf(csrf -> csrf.disable())
                .authorizeHttpRequests(auth -> auth.anyRequest().permitAll());
            return http.build();
        }
    }

    @Autowired private MockMvc mockMvc;
    @Autowired private ObjectMapper objectMapper;

    @MockBean private JwtUtil jwtUtil;
    @MockBean private CommunityService communityService;
    @MockBean private DiscussionService discussionService;
    @MockBean private ModerationService moderationService;

    private static final UUID TEST_USER_ID = UUID.fromString("d3b07384-d113-4956-9d8e-1282ec4567e9");

    private UsernamePasswordAuthenticationToken adminAuth() {
        return new UsernamePasswordAuthenticationToken(
                new AuthPrincipal(TEST_USER_ID, UserRole.ADMIN, "jti", 9999999999L),
                null,
                List.of(new SimpleGrantedAuthority("ROLE_ADMIN"))
        );
    }

    // ─── GET /api/communities ─────────────────────────────────────────────────

    @Test
    void getCommunities_returns200WithList() throws Exception {
        CommunityResponse c = sampleCommunity(1L, "Cairo Music Lovers");
        when(communityService.getCommunities(any(), any(), any(), any())).thenReturn(List.of(c));

        mockMvc.perform(get("/api/communities"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data[0].name").value("Cairo Music Lovers"))
                .andExpect(jsonPath("$.data[0].memberCount").value(1000));
    }

    @Test
    void getCommunities_supportsSearchAndCategoryParams() throws Exception {
        when(communityService.getCommunities(eq("music"), eq("Music"), eq("popular"), eq(TEST_USER_ID)))
                .thenReturn(Collections.emptyList());

        mockMvc.perform(get("/api/communities?search=music&category=Music&sort=popular&userId=" + TEST_USER_ID))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data").isArray());
    }

    // ─── GET /api/communities/{id} ────────────────────────────────────────────

    @Test
    void getCommunity_returns200WithCommunity() throws Exception {
        CommunityResponse c = sampleCommunity(1L, "Cairo Music Lovers");
        when(communityService.getCommunity(eq(1L), any())).thenReturn(c);

        mockMvc.perform(get("/api/communities/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.id").value(1))
                .andExpect(jsonPath("$.data.category").value("Music"));
    }

    @Test
    void getCommunity_returns404WhenNotFound() throws Exception {
        when(communityService.getCommunity(eq(99L), any()))
                .thenThrow(new com.eventra.backend.common.exception.ResourceNotFoundException("Not found"));

        mockMvc.perform(get("/api/communities/99"))
                .andExpect(status().isNotFound());
    }

    // ─── POST /api/communities ────────────────────────────────────────────────

    @Test
    void createCommunity_returns200WithNewCommunity() throws Exception {
        CommunityResponse c = sampleCommunity(2L, "New Community");
        when(communityService.createCommunity(any())).thenReturn(c);

        CreateCommunityRequest req = new CreateCommunityRequest();
        req.setName("New Community");
        req.setCategory("Tech");

        mockMvc.perform(post("/api/communities")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.name").value("New Community"));
    }

    @Test
    void createCommunity_returns400WhenNameMissing() throws Exception {
        mockMvc.perform(post("/api/communities")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"category\":\"Tech\"}"))
                .andExpect(status().isBadRequest());
    }

    // ─── POST /api/communities/{id}/join ─────────────────────────────────────

    @Test
    void joinCommunity_returns200AndJoinedTrue() throws Exception {
        CommunityResponse c = sampleCommunity(1L, "Cairo Music Lovers");
        c.setJoined(true);
        c.setMemberCount(1001L);
        when(communityService.joinCommunity(eq(1L), any())).thenReturn(c);

        JoinCommunityRequest req = new JoinCommunityRequest();
        req.setUserId(TEST_USER_ID);
        req.setDisplayName("Bob");

        mockMvc.perform(post("/api/communities/1/join")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.joined").value(true))
                .andExpect(jsonPath("$.data.memberCount").value(1001));
    }

    // ─── DELETE /api/communities/{id}/leave ──────────────────────────────────

    @Test
    void leaveCommunity_returns200AndJoinedFalse() throws Exception {
        CommunityResponse c = sampleCommunity(1L, "Cairo Music Lovers");
        c.setJoined(false);
        c.setMemberCount(999L);
        when(communityService.leaveCommunity(eq(1L), eq(TEST_USER_ID))).thenReturn(c);

        mockMvc.perform(delete("/api/communities/1/leave").with(authentication(adminAuth())))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.joined").value(false))
                .andExpect(jsonPath("$.data.memberCount").value(999));
    }

    // ─── GET /api/communities/{id}/members ───────────────────────────────────

    @Test
    void getMembers_returns200WithMemberList() throws Exception {
        CommunityMemberResponse member = new CommunityMemberResponse();
        member.setUserId(TEST_USER_ID);
        member.setDisplayName("Bob");

        when(communityService.getMembers(eq(1L), anyInt(), anyInt())).thenReturn(List.of(member));

        mockMvc.perform(get("/api/communities/1/members"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data[0].displayName").value("Bob"));
    }

    // ─── GET /api/communities/{id}/discussions ───────────────────────────────

    @Test
    void getDiscussions_returns200WithList() throws Exception {
        DiscussionResponse d = sampleDiscussion(10L);
        when(discussionService.getDiscussions(1L)).thenReturn(List.of(d));

        mockMvc.perform(get("/api/communities/1/discussions"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data[0].title").value("Best jazz venues?"))
                .andExpect(jsonPath("$.data[0].hot").value(false));
    }

    // ─── POST /api/communities/{id}/discussions ───────────────────────────────

    @Test
    void createDiscussion_returns200WithNewDiscussion() throws Exception {
        DiscussionResponse d = sampleDiscussion(10L);
        when(discussionService.createDiscussion(eq(1L), any())).thenReturn(d);

        CreateDiscussionRequest req = new CreateDiscussionRequest();
        req.setAuthorId(TEST_USER_ID);
        req.setAuthorName("Alice");
        req.setTitle("Best jazz venues?");

        mockMvc.perform(post("/api/communities/1/discussions")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.id").value(10));
    }

    // ─── POST /api/communities/{communityId}/discussions/{id}/replies ─────────

    @Test
    void addReply_returns200WithReply() throws Exception {
        DiscussionReplyResponse reply = new DiscussionReplyResponse();
        reply.setId(1L);
        reply.setDiscussionId(10L);
        reply.setAuthorId(TEST_USER_ID);
        reply.setAuthorName("Bob");
        reply.setContent("Great suggestion!");
        reply.setCreatedAt(LocalDateTime.now());

        when(discussionService.addReply(eq(1L), eq(10L), any())).thenReturn(reply);

        CreateDiscussionReplyRequest req = new CreateDiscussionReplyRequest();
        req.setAuthorId(TEST_USER_ID);
        req.setAuthorName("Bob");
        req.setContent("Great suggestion!");

        mockMvc.perform(post("/api/communities/1/discussions/10/replies")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.content").value("Great suggestion!"))
                .andExpect(jsonPath("$.data.authorName").value("Bob"));
    }

    // ─── GET /api/admin/community/flagged ─────────────────────────────────────

    @Test
    void getFlaggedContent_returns200WithStats() throws Exception {
        FlaggedContentResponse flagged = new FlaggedContentResponse();
        flagged.setId(1L);
        flagged.setContentType(ContentType.DISCUSSION);
        flagged.setReason("Spam");
        flagged.setStatus(FlagStatus.PENDING);

        ModerationStatsResponse stats = new ModerationStatsResponse(1L, 0L, 1L, List.of(flagged));
        when(moderationService.getFlaggedContent()).thenReturn(stats);

        mockMvc.perform(get("/api/admin/community/flagged").with(authentication(adminAuth())))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.pendingFlags").value(1))
                .andExpect(jsonPath("$.data.flaggedItems[0].reason").value("Spam"));
    }

    // ─── POST /api/admin/community/flagged/{id}/approve ──────────────────────

    @Test
    void approveContent_returns200() throws Exception {
        FlaggedContentResponse flagged = new FlaggedContentResponse();
        flagged.setId(1L);
        flagged.setStatus(FlagStatus.APPROVED);

        when(moderationService.approveContent(eq(1L), any())).thenReturn(flagged);

        mockMvc.perform(post("/api/admin/community/flagged/1/approve").with(authentication(adminAuth())))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.status").value("APPROVED"));
    }

    // ─── Helpers ──────────────────────────────────────────────────────────────

    private CommunityResponse sampleCommunity(Long id, String name) {
        CommunityResponse c = new CommunityResponse();
        c.setId(id);
        c.setName(name);
        c.setCategory("Music");
        c.setMemberCount(1000L);
        c.setEventCount(20L);
        c.setJoined(false);
        c.setCreatedAt(LocalDateTime.now());
        return c;
    }

    private DiscussionResponse sampleDiscussion(Long id) {
        DiscussionResponse d = new DiscussionResponse();
        d.setId(id);
        d.setCommunityId(1L);
        d.setTitle("Best jazz venues?");
        d.setContent("Looking for recommendations");
        d.setAuthorId(TEST_USER_ID);
        d.setAuthorName("Alice");
        d.setReplyCount(0);
        d.setHot(false);
        d.setCreatedAt(LocalDateTime.now());
        return d;
    }
}

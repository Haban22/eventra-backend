package com.eventra.backend.module.analytics;

import com.eventra.backend.module.analytics.client.AiServiceClient;
import com.eventra.backend.module.analytics.controller.RecommendationController;
import com.eventra.backend.module.analytics.service.NLPSearchEngine;
import com.eventra.backend.module.analytics.service.RecommendationService;
import com.eventra.backend.module.auth.security.JwtUtil;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;
import java.util.Map;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(RecommendationController.class)
@Import(AiControllerTest.TestSecurityConfig.class)
class AiControllerTest {

    @TestConfiguration
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
    @MockBean private RecommendationService recommendationService;
    @MockBean private NLPSearchEngine nlpSearchEngine;

    // ─── POST /api/ai/recommendations/user ───────────────────────────────────

    @Test
    void recommendForUser_returns200WithResults() throws Exception {
        List<Map<String, Object>> results = List.of(
                Map.of("event", Map.of("id", 1, "title", "AI Summit"), "score", 0.85),
                Map.of("event", Map.of("id", 3, "title", "Tech Expo"), "score", 0.72)
        );
        when(recommendationService.getForUser(any())).thenReturn(results);

        Map<String, Object> body = Map.of(
                "interests", List.of("Technology", "Business"),
                "interactions", List.of(),
                "limit", 10
        );

        mockMvc.perform(post("/api/ai/recommendations/user")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(body)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data[0].score").value(0.85))
                .andExpect(jsonPath("$.data[0].event.title").value("AI Summit"))
                .andExpect(jsonPath("$.data[1].score").value(0.72));
    }

    @Test
    void recommendForUser_returns400WhenInterestsMissing() throws Exception {
        Map<String, Object> body = Map.of("limit", 10);

        mockMvc.perform(post("/api/ai/recommendations/user")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(body)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void recommendForUser_returns503WhenAiDown() throws Exception {
        when(recommendationService.getForUser(any()))
                .thenThrow(new AiServiceClient.AiServiceUnavailableException("AI recommendation service unavailable", new RuntimeException()));

        Map<String, Object> body = Map.of(
                "interests", List.of("Technology"),
                "interactions", List.of()
        );

        mockMvc.perform(post("/api/ai/recommendations/user")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(body)))
                .andExpect(status().isServiceUnavailable())
                .andExpect(jsonPath("$.error").value("AI_SERVICE_UNAVAILABLE"));
    }

    // ─── GET /api/ai/recommendations/events/{eventId} ────────────────────────

    @Test
    void recommendSimilar_returns200WithTopMatches() throws Exception {
        List<Map<String, Object>> results = List.of(
                Map.of("event", Map.of("id", 2, "title", "Data Science Week"), "score", 0.91),
                Map.of("event", Map.of("id", 5, "title", "Cloud Conf"), "score", 0.78)
        );
        when(recommendationService.getSimilar(eq(1), anyInt())).thenReturn(results);

        mockMvc.perform(get("/api/ai/recommendations/events/1?limit=2"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data[0].score").value(0.91))
                .andExpect(jsonPath("$.data[0].event.title").value("Data Science Week"));
    }

    @Test
    void recommendSimilar_returns503WhenAiDown() throws Exception {
        when(recommendationService.getSimilar(anyInt(), anyInt()))
                .thenThrow(new AiServiceClient.AiServiceUnavailableException("AI similarity service unavailable", new RuntimeException()));

        mockMvc.perform(get("/api/ai/recommendations/events/99"))
                .andExpect(status().isServiceUnavailable())
                .andExpect(jsonPath("$.error").value("AI_SERVICE_UNAVAILABLE"));
    }

    // ─── POST /api/ai/search ─────────────────────────────────────────────────

    @Test
    void search_returns200WithResults() throws Exception {
        List<Map<String, Object>> results = List.of(
                Map.of("event", Map.of("id", 1, "title", "AI Summit"), "score", 0.95)
        );
        when(nlpSearchEngine.search(any())).thenReturn(results);

        Map<String, Object> body = Map.of("query", "technology", "limit", 5);

        mockMvc.perform(post("/api/ai/search")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(body)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data[0].event.title").value("AI Summit"));
    }

    @Test
    void search_returns400WhenQueryMissing() throws Exception {
        Map<String, Object> body = Map.of("limit", 5);

        mockMvc.perform(post("/api/ai/search")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(body)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void search_returns200WithNoMatchesMessage() throws Exception {
        when(nlpSearchEngine.search(any())).thenReturn(Map.of("message", "No matching events found."));

        mockMvc.perform(post("/api/ai/search")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of("query", "quantum entanglement"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.message").value("No matching events found."));
    }

    // ─── POST /api/ai/sentiment ───────────────────────────────────────────────

    @Test
    void sentiment_returns200WithPositiveResult() throws Exception {
        Map<String, Object> result = Map.of("label", "POSITIVE", "score", 0.997);
        when(recommendationService.analyzeSentiment(anyString())).thenReturn(result);

        Map<String, Object> body = Map.of("text", "This event was absolutely amazing!");

        mockMvc.perform(post("/api/ai/sentiment")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(body)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.label").value("POSITIVE"))
                .andExpect(jsonPath("$.data.score").value(0.997));
    }

    @Test
    void sentiment_returns200WithNegativeResult() throws Exception {
        Map<String, Object> result = Map.of("label", "NEGATIVE", "score", 0.985);
        when(recommendationService.analyzeSentiment(anyString())).thenReturn(result);

        Map<String, Object> body = Map.of("text", "Terrible event, total waste of time.");

        mockMvc.perform(post("/api/ai/sentiment")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(body)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.label").value("NEGATIVE"));
    }

    @Test
    void sentiment_returns400WhenTextMissing() throws Exception {
        Map<String, Object> body = Map.of();

        mockMvc.perform(post("/api/ai/sentiment")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(body)))
                .andExpect(status().isBadRequest());
    }
}

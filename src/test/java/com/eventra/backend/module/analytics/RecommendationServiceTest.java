package com.eventra.backend.module.analytics;

import com.eventra.backend.module.analytics.client.AiServiceClient;
import com.eventra.backend.module.analytics.dto.UserRecommendationRequest;
import com.eventra.backend.module.analytics.service.NLPSearchEngine;
import com.eventra.backend.module.analytics.dto.SearchRequest;
import com.eventra.backend.module.analytics.service.RecommendationService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RecommendationServiceTest {

    @Mock AiServiceClient aiServiceClient;
    @InjectMocks RecommendationService recommendationService;
    @InjectMocks NLPSearchEngine nlpSearchEngine;

    private UserRecommendationRequest req;

    @BeforeEach
    void setUp() {
        nlpSearchEngine = new NLPSearchEngine(aiServiceClient);
        req = new UserRecommendationRequest();
        req.setInterests(List.of("Technology", "Business"));
        req.setInteractions(List.of());
        req.setLimit(10);
    }

    // ─── RecommendationService ────────────────────────────────────────────────

    @Test
    void getForUser_delegatesToAiClient() {
        List<Map<String, Object>> expected = List.of(
                Map.of("event", Map.of("id", 1), "score", 0.85)
        );
        when(aiServiceClient.getUserRecommendations(
                List.of("Technology", "Business"), List.of(), 10
        )).thenReturn(expected);

        List<Map<String, Object>> result = recommendationService.getForUser(req);

        assertThat(result).isEqualTo(expected);
        verify(aiServiceClient).getUserRecommendations(List.of("Technology", "Business"), List.of(), 10);
    }

    @Test
    void getSimilar_delegatesToAiClient() {
        List<Map<String, Object>> expected = List.of(
                Map.of("event", Map.of("id", 2), "score", 0.91)
        );
        when(aiServiceClient.getSimilarEvents(1, 3)).thenReturn(expected);

        List<Map<String, Object>> result = recommendationService.getSimilar(1, 3);

        assertThat(result).isEqualTo(expected);
        verify(aiServiceClient).getSimilarEvents(1, 3);
    }

    @Test
    void getSimilar_usesProvidedLimit() {
        when(aiServiceClient.getSimilarEvents(5, 10)).thenReturn(List.of());

        recommendationService.getSimilar(5, 10);

        verify(aiServiceClient).getSimilarEvents(5, 10);
    }

    @Test
    void analyzeSentiment_delegatesToAiClient() {
        Map<String, Object> expected = Map.of("label", "POSITIVE", "score", 0.99);
        when(aiServiceClient.analyzeSentiment("Great event!")).thenReturn(expected);

        Map<String, Object> result = recommendationService.analyzeSentiment("Great event!");

        assertThat(result).isEqualTo(expected);
        verify(aiServiceClient).analyzeSentiment("Great event!");
    }

    @Test
    void getForUser_propagatesAiServiceException() {
        when(aiServiceClient.getUserRecommendations(any(), any(), anyInt()))
                .thenThrow(new AiServiceClient.AiServiceUnavailableException("down", new RuntimeException()));

        assertThatThrownBy(() -> recommendationService.getForUser(req))
                .isInstanceOf(AiServiceClient.AiServiceUnavailableException.class)
                .hasMessageContaining("down");
    }

    // ─── NLPSearchEngine ─────────────────────────────────────────────────────

    @Test
    void search_delegatesToAiClient() {
        SearchRequest searchReq = new SearchRequest();
        searchReq.setQuery("technology");
        searchReq.setLimit(5);

        List<Map<String, Object>> expected = List.of(
                Map.of("event", Map.of("id", 1), "score", 0.95)
        );
        when(aiServiceClient.searchEvents("technology", 5)).thenReturn(expected);

        Object result = nlpSearchEngine.search(searchReq);

        assertThat(result).isEqualTo(expected);
        verify(aiServiceClient).searchEvents("technology", 5);
    }

    @Test
    void search_returnsNoMatchesMessage() {
        SearchRequest searchReq = new SearchRequest();
        searchReq.setQuery("quantum entanglement");
        searchReq.setLimit(5);

        Map<String, Object> noMatch = Map.of("message", "No matching events found.");
        when(aiServiceClient.searchEvents("quantum entanglement", 5)).thenReturn(noMatch);

        Object result = nlpSearchEngine.search(searchReq);

        assertThat(result).isEqualTo(noMatch);
    }

    @Test
    void search_propagatesAiServiceException() {
        SearchRequest searchReq = new SearchRequest();
        searchReq.setQuery("music");
        searchReq.setLimit(5);

        when(aiServiceClient.searchEvents(anyString(), anyInt()))
                .thenThrow(new AiServiceClient.AiServiceUnavailableException("down", new RuntimeException()));

        assertThatThrownBy(() -> nlpSearchEngine.search(searchReq))
                .isInstanceOf(AiServiceClient.AiServiceUnavailableException.class);
    }
}

package com.eventra.backend.module.analytics.client;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

import java.util.List;
import java.util.Map;

@Component
public class AiServiceClient {

    private final RestClient restClient;

    public AiServiceClient(@Value("${eventra.ai.url:http://localhost:8001}") String aiUrl) {
        this.restClient = RestClient.builder()
                .baseUrl(aiUrl)
                .build();
    }

    public List<Map<String, Object>> getUserRecommendations(
            List<String> interests,
            List<Map<String, Object>> interactions,
            int limit
    ) {
        try {
            return restClient.post()
                    .uri("/recommendations/user")
                    .body(Map.of("interests", interests, "interactions", interactions, "limit", limit))
                    .retrieve()
                    .body(new ParameterizedTypeReference<>() {});
        } catch (RestClientException e) {
            throw new AiServiceUnavailableException("AI recommendation service unavailable", e);
        }
    }

    public List<Map<String, Object>> getSimilarEvents(int eventId, int limit) {
        try {
            return restClient.get()
                    .uri("/recommendations/events/{eventId}?limit={limit}", eventId, limit)
                    .retrieve()
                    .body(new ParameterizedTypeReference<>() {});
        } catch (RestClientException e) {
            throw new AiServiceUnavailableException("AI similarity service unavailable", e);
        }
    }

    public Map<String, Object> analyzeSentiment(String text) {
        try {
            return restClient.post()
                    .uri("/sentiment")
                    .body(Map.of("text", text))
                    .retrieve()
                    .body(new ParameterizedTypeReference<>() {});
        } catch (RestClientException e) {
            throw new AiServiceUnavailableException("AI sentiment service unavailable", e);
        }
    }

    public Object searchEvents(String query, int limit) {
        try {
            return restClient.post()
                    .uri("/search")
                    .body(Map.of("query", query, "limit", limit))
                    .retrieve()
                    .body(new ParameterizedTypeReference<>() {});
        } catch (RestClientException e) {
            throw new AiServiceUnavailableException("AI search service unavailable", e);
        }
    }

    public static class AiServiceUnavailableException extends RuntimeException {
        public AiServiceUnavailableException(String message, Throwable cause) {
            super(message, cause);
        }
    }
}

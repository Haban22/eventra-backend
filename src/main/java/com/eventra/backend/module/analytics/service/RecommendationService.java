package com.eventra.backend.module.analytics.service;

import com.eventra.backend.module.analytics.client.AiServiceClient;
import com.eventra.backend.module.analytics.dto.UserRecommendationRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class RecommendationService {

    private final AiServiceClient aiServiceClient;

    public List<Map<String, Object>> getForUser(UserRecommendationRequest req) {
        return aiServiceClient.getUserRecommendations(
                req.getInterests(),
                req.getInteractions(),
                req.getLimit()
        );
    }

    public List<Map<String, Object>> getSimilar(int eventId, int limit) {
        return aiServiceClient.getSimilarEvents(eventId, limit);
    }

    public Map<String, Object> analyzeSentiment(String text) {
        return aiServiceClient.analyzeSentiment(text);
    }
}

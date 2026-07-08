package com.eventra.backend.module.analytics.controller;

import com.eventra.backend.common.response.ApiResponse;
import com.eventra.backend.module.analytics.dto.SearchRequest;
import com.eventra.backend.module.analytics.dto.SentimentRequest;
import com.eventra.backend.module.analytics.dto.UserRecommendationRequest;
import com.eventra.backend.module.analytics.service.NLPSearchEngine;
import com.eventra.backend.module.analytics.service.RecommendationService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/ai")
@RequiredArgsConstructor
public class RecommendationController {

    private final RecommendationService recommendationService;
    private final NLPSearchEngine nlpSearchEngine;

    @PostMapping("/recommendations/user")
    public ResponseEntity<ApiResponse<List<Map<String, Object>>>> recommendForUser(
            @Valid @RequestBody UserRecommendationRequest req
    ) {
        return ResponseEntity.ok(ApiResponse.success(recommendationService.getForUser(req)));
    }

    @GetMapping("/recommendations/events/{eventId}")
    public ResponseEntity<ApiResponse<List<Map<String, Object>>>> recommendSimilar(
            @PathVariable int eventId,
            @RequestParam(defaultValue = "3") int limit
    ) {
        return ResponseEntity.ok(ApiResponse.success(recommendationService.getSimilar(eventId, limit)));
    }

    @PostMapping("/search")
    public ResponseEntity<ApiResponse<Object>> search(
            @Valid @RequestBody SearchRequest req
    ) {
        return ResponseEntity.ok(ApiResponse.success(nlpSearchEngine.search(req)));
    }

    @PostMapping("/sentiment")
    public ResponseEntity<ApiResponse<Map<String, Object>>> sentiment(
            @Valid @RequestBody SentimentRequest req
    ) {
        return ResponseEntity.ok(ApiResponse.success(recommendationService.analyzeSentiment(req.getText())));
    }

    @GetMapping("/feedback/event/{eventId}")
    public ResponseEntity<ApiResponse<List<Map<String, Object>>>> getEventFeedback(
            @PathVariable java.util.UUID eventId
    ) {
        return ResponseEntity.ok(ApiResponse.success(recommendationService.getEventFeedback(eventId)));
    }

    @PostMapping("/feedback/analyze")
    public ResponseEntity<ApiResponse<Map<String, Object>>> analyzeFeedback(
            @RequestBody Map<String, String> req
    ) {
        java.util.UUID eventId = java.util.UUID.fromString(req.get("eventId"));
        return ResponseEntity.ok(ApiResponse.success(recommendationService.analyzeFeedback(eventId)));
    }

    @PostMapping("/feedback/predict-attendance-detailed")
    public ResponseEntity<ApiResponse<Map<String, Object>>> predictAttendanceDetailed(
            @RequestBody Map<String, Object> req
    ) {
        return ResponseEntity.ok(ApiResponse.success(recommendationService.predictAttendanceDetailed(req)));
    }

    @PostMapping("/predict-attendance")
    public ResponseEntity<ApiResponse<Map<String, Object>>> predictAttendance(
            @RequestBody Map<String, Object> req
    ) {
        return ResponseEntity.ok(ApiResponse.success(recommendationService.predictAttendanceDetailed(req)));
    }
}

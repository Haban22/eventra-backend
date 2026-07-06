package com.eventra.backend.module.analytics;

import com.eventra.backend.module.analytics.client.AiServiceClient;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.client.RestClientTest;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.web.client.MockRestServiceServer;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.*;
import static org.springframework.test.web.client.response.MockRestResponseCreators.*;

@RestClientTest(AiServiceClient.class)
class AiServiceClientTest {

    @Autowired
    private AiServiceClient aiServiceClient;

    @Autowired
    private MockRestServiceServer server;

    @Autowired
    private ObjectMapper objectMapper;

    private static final String BASE_URL = "http://localhost:8001";

    @Test
    void getUserRecommendations_returnsListOnSuccess() throws Exception {
        List<Map<String, Object>> expectedResponse = List.of(
                Map.of("eventId", 123, "score", 0.95)
        );

        server.expect(requestTo(BASE_URL + "/recommendations/user"))
                .andExpect(method(org.springframework.http.HttpMethod.POST))
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.interests[0]").value("Technology"))
                .andExpect(jsonPath("$.limit").value(5))
                .andRespond(withSuccess(objectMapper.writeValueAsString(expectedResponse), MediaType.APPLICATION_JSON));

        List<Map<String, Object>> result = aiServiceClient.getUserRecommendations(
                List.of("Technology"), List.of(), 5
        );

        assertThat(result).isEqualTo(expectedResponse);
    }

    @Test
    void getUserRecommendations_throwsExceptionOnFailure() {
        server.expect(requestTo(BASE_URL + "/recommendations/user"))
                .andRespond(withStatus(HttpStatus.INTERNAL_SERVER_ERROR));

        assertThatThrownBy(() -> aiServiceClient.getUserRecommendations(List.of("Technology"), List.of(), 5))
                .isInstanceOf(AiServiceClient.AiServiceUnavailableException.class)
                .hasMessageContaining("AI recommendation service unavailable");
    }

    @Test
    void getSimilarEvents_returnsListOnSuccess() throws Exception {
        List<Map<String, Object>> expectedResponse = List.of(
                Map.of("eventId", 456, "score", 0.88)
        );

        server.expect(requestTo(BASE_URL + "/recommendations/events/1?limit=3"))
                .andExpect(method(org.springframework.http.HttpMethod.GET))
                .andRespond(withSuccess(objectMapper.writeValueAsString(expectedResponse), MediaType.APPLICATION_JSON));

        List<Map<String, Object>> result = aiServiceClient.getSimilarEvents(1, 3);

        assertThat(result).isEqualTo(expectedResponse);
    }

    @Test
    void getSimilarEvents_throwsExceptionOnFailure() {
        server.expect(requestTo(BASE_URL + "/recommendations/events/1?limit=3"))
                .andRespond(withStatus(HttpStatus.BAD_REQUEST));

        assertThatThrownBy(() -> aiServiceClient.getSimilarEvents(1, 3))
                .isInstanceOf(AiServiceClient.AiServiceUnavailableException.class)
                .hasMessageContaining("AI similarity service unavailable");
    }

    @Test
    void analyzeSentiment_returnsMapOnSuccess() throws Exception {
        Map<String, Object> expectedResponse = Map.of(
                "label", "POSITIVE",
                "score", 0.99
        );

        server.expect(requestTo(BASE_URL + "/sentiment"))
                .andExpect(method(org.springframework.http.HttpMethod.POST))
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.text").value("Hello world"))
                .andRespond(withSuccess(objectMapper.writeValueAsString(expectedResponse), MediaType.APPLICATION_JSON));

        Map<String, Object> result = aiServiceClient.analyzeSentiment("Hello world");

        assertThat(result).isEqualTo(expectedResponse);
    }

    @Test
    void analyzeSentiment_throwsExceptionOnFailure() {
        server.expect(requestTo(BASE_URL + "/sentiment"))
                .andRespond(withStatus(HttpStatus.NOT_FOUND));

        assertThatThrownBy(() -> aiServiceClient.analyzeSentiment("Hello world"))
                .isInstanceOf(AiServiceClient.AiServiceUnavailableException.class)
                .hasMessageContaining("AI sentiment service unavailable");
    }

    @Test
    void searchEvents_returnsObjectOnSuccess() throws Exception {
        List<Map<String, Object>> expectedResponse = List.of(
                Map.of("eventId", 789, "score", 0.75)
        );

        server.expect(requestTo(BASE_URL + "/search"))
                .andExpect(method(org.springframework.http.HttpMethod.POST))
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.query").value("jazz"))
                .andExpect(jsonPath("$.limit").value(10))
                .andRespond(withSuccess(objectMapper.writeValueAsString(expectedResponse), MediaType.APPLICATION_JSON));

        Object result = aiServiceClient.searchEvents("jazz", 10);

        assertThat(result).isEqualTo(expectedResponse);
    }

    @Test
    void searchEvents_throwsExceptionOnFailure() {
        server.expect(requestTo(BASE_URL + "/search"))
                .andRespond(withStatus(HttpStatus.BAD_GATEWAY));

        assertThatThrownBy(() -> aiServiceClient.searchEvents("jazz", 10))
                .isInstanceOf(AiServiceClient.AiServiceUnavailableException.class)
                .hasMessageContaining("AI search service unavailable");
    }
}

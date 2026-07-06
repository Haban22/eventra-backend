package com.eventra.backend.module.analytics.service;

import com.eventra.backend.module.analytics.client.AiServiceClient;
import com.eventra.backend.module.analytics.dto.SearchRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class NLPSearchEngine {

    private final AiServiceClient aiServiceClient;

    public Object search(SearchRequest req) {
        return aiServiceClient.searchEvents(req.getQuery(), req.getLimit());
    }
}

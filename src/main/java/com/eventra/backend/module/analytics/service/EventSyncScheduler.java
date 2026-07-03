package com.eventra.backend.module.analytics.service;

import com.eventra.backend.module.analytics.client.AiServiceClient;
import com.eventra.backend.module.event.entity.Event;
import com.eventra.backend.module.event.enums.EventStatus;
import com.eventra.backend.module.event.repository.EventRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class EventSyncScheduler {

    private final EventRepository eventRepository;
    private final AiServiceClient aiServiceClient;

    /**
     * Sync all published events to the AI microservice on application startup.
     */
    @EventListener(ApplicationReadyEvent.class)
    @Transactional(readOnly = true)
    public void syncOnStartup() {
        log.info("Application ready. Initializing database event synchronization with AI Service...");
        syncEvents();
    }

    /**
     * Periodically sync events every 15 minutes to ensure consistency.
     */
    @Scheduled(fixedDelay = 900000) // 15 minutes in milliseconds
    @Transactional(readOnly = true)
    public void scheduledSync() {
        log.info("Starting scheduled event synchronization with AI Service...");
        syncEvents();
    }

    @Transactional(readOnly = true)
    public synchronized void syncEvents() {
        try {
            List<Event> publishedEvents = eventRepository.findByStatus(EventStatus.PUBLISHED);
            List<Map<String, Object>> syncPayload = new ArrayList<>();

            for (Event event : publishedEvents) {
                Map<String, Object> map = new HashMap<>();
                map.put("id", event.getId().toString());
                map.put("title", event.getTitle());
                map.put("description", event.getDescription() != null ? event.getDescription() : "");
                map.put("category", event.getCategory() != null ? event.getCategory().getName() : "Other");
                
                // Extract city or fallback to default
                String city = "Cairo";
                if (event.getLocation() != null && event.getLocation().getCity() != null) {
                    city = event.getLocation().getCity();
                }
                map.put("location", city);
                
                String dateStr = event.getDateTime() != null 
                        ? event.getDateTime().toString().split("T")[0] 
                        : "";
                map.put("date", dateStr);

                syncPayload.add(map);
            }

            log.info("Sending {} published events to AI microservice for sync...", syncPayload.size());
            Map<String, Object> result = aiServiceClient.syncEvents(syncPayload);
            log.info("AI Microservice sync response: {}", result);
        } catch (Exception e) {
            log.error("Failed to sync events with AI service: {}", e.getMessage());
        }
    }
}

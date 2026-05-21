package com.eventra.backend.module.event.controller;

import com.eventra.backend.module.event.dto.request.EventSearchRequest;
import com.eventra.backend.module.event.dto.response.EventResponse;
import com.eventra.backend.module.event.dto.response.EventSummaryResponse;
import com.eventra.backend.module.event.service.EventService;
import org.springframework.data.domain.Page;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/events")
public class EventController {

    private final EventService eventService;

    public EventController(EventService eventService) {
        this.eventService = eventService;
    }

    @GetMapping("/{id}")
    public EventResponse getEvent(@PathVariable UUID id) {
        return eventService.getEvent(id);
    }

    @GetMapping
    public Page<EventSummaryResponse> searchEvents(
            @RequestParam(required = false) UUID categoryId,
            @RequestParam(required = false) String city,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Instant from,
            @RequestParam(required = false) Instant to,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        return eventService.searchEvents(
                new EventSearchRequest(categoryId, city, keyword, from, to),
                page,
                size);
    }
}
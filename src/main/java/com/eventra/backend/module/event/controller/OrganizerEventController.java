package com.eventra.backend.module.event.controller;

import com.eventra.backend.module.auth.dto.response.MessageResponse;
import com.eventra.backend.module.auth.security.AuthPrincipal;
import com.eventra.backend.module.event.dto.request.EventRequest;
import com.eventra.backend.module.event.dto.response.EventResponse;
import com.eventra.backend.module.event.service.EventService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/organizer/events")
@PreAuthorize("hasRole('ORGANIZER')")
public class OrganizerEventController {

    private final EventService eventService;

    public OrganizerEventController(EventService eventService) {
        this.eventService = eventService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public EventResponse createEvent(
            @AuthenticationPrincipal AuthPrincipal principal,
            @Valid @RequestBody EventRequest request) {
        return eventService.createEvent(principal.userId(), request);
    }

    @PutMapping("/{id}")
    public EventResponse updateEvent(
            @AuthenticationPrincipal AuthPrincipal principal,
            @PathVariable UUID id,
            @Valid @RequestBody EventRequest request) {
        return eventService.updateEvent(principal.userId(), id, request);
    }

    @PostMapping("/{id}/publish")
    public EventResponse publishEvent(
            @AuthenticationPrincipal AuthPrincipal principal,
            @PathVariable UUID id) {
        return eventService.publishEvent(principal.userId(), id);
    }

    @PostMapping("/{id}/duplicate")
    @ResponseStatus(HttpStatus.CREATED)
    public EventResponse duplicateEvent(
            @AuthenticationPrincipal AuthPrincipal principal,
            @PathVariable UUID id) {
        return eventService.duplicateEvent(principal.userId(), id);
    }

    @DeleteMapping("/{id}")
    public MessageResponse cancelEvent(
            @AuthenticationPrincipal AuthPrincipal principal,
            @PathVariable UUID id) {
        eventService.cancelEvent(principal.userId(), id);
        return new MessageResponse("Event cancelled");
    }

    @GetMapping
    public List<EventResponse> myEvents(
            @AuthenticationPrincipal AuthPrincipal principal) {
        return eventService.getOrganizerEvents(principal.userId());
    }
}
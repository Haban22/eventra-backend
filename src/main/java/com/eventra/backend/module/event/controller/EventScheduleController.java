package com.eventra.backend.module.event.controller;

import com.eventra.backend.module.auth.security.AuthPrincipal;
import com.eventra.backend.module.event.dto.request.ScheduleItemRequest;
import com.eventra.backend.module.event.dto.response.ScheduleItemResponse;
import com.eventra.backend.module.event.service.ScheduleService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1")
public class EventScheduleController {

    private final ScheduleService scheduleService;

    public EventScheduleController(ScheduleService scheduleService) {
        this.scheduleService = scheduleService;
    }

    @GetMapping("/events/{eventId}/schedule")
    public List<ScheduleItemResponse> getEventSchedule(@PathVariable UUID eventId) {
        return scheduleService.getScheduleByEventId(eventId);
    }

    @PostMapping("/organizer/events/{eventId}/schedule")
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasRole('ORGANIZER')")
    public ScheduleItemResponse addScheduleItem(
            @PathVariable UUID eventId,
            @AuthenticationPrincipal AuthPrincipal principal,
            @Valid @RequestBody ScheduleItemRequest request) {
        return scheduleService.addScheduleItem(eventId, principal.userId(), request);
    }

    @PutMapping("/organizer/events/{eventId}/schedule/{itemId}")
    @PreAuthorize("hasRole('ORGANIZER')")
    public ScheduleItemResponse updateScheduleItem(
            @PathVariable UUID eventId,
            @PathVariable UUID itemId,
            @AuthenticationPrincipal AuthPrincipal principal,
            @Valid @RequestBody ScheduleItemRequest request) {
        return scheduleService.updateScheduleItem(itemId, principal.userId(), request);
    }

    @DeleteMapping("/organizer/events/{eventId}/schedule/{itemId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PreAuthorize("hasRole('ORGANIZER')")
    public void deleteScheduleItem(
            @PathVariable UUID eventId,
            @PathVariable UUID itemId,
            @AuthenticationPrincipal AuthPrincipal principal) {
        scheduleService.deleteScheduleItem(itemId, principal.userId());
    }
}

package com.eventra.backend.module.calendar.controller;

import com.eventra.backend.module.auth.security.AuthPrincipal;
import com.eventra.backend.module.calendar.dto.CreatePersonalEventRequest;
import com.eventra.backend.module.calendar.dto.PersonalEventResponse;
import com.eventra.backend.module.calendar.service.PersonalEventService;
import jakarta.validation.Valid;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/calendar")
public class PersonalEventController {
    private final PersonalEventService service;

    public PersonalEventController(PersonalEventService service) {
        this.service = service;
    }

    @GetMapping("/me")
    public List<PersonalEventResponse> getMyEvents(@AuthenticationPrincipal AuthPrincipal principal) {
        return service.getMyEvents(principal.userId());
    }

    @PostMapping
    public PersonalEventResponse create(@AuthenticationPrincipal AuthPrincipal principal, @Valid @RequestBody CreatePersonalEventRequest request) {
        return service.create(principal.userId(), request);
    }

    @DeleteMapping("/{id}")
    public void delete(@AuthenticationPrincipal AuthPrincipal principal, @PathVariable UUID id) {
        service.delete(principal.userId(), id);
    }
}

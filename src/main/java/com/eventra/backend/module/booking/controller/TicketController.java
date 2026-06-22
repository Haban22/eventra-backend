package com.eventra.backend.module.booking.controller;

import com.eventra.backend.module.auth.security.AuthPrincipal;
import com.eventra.backend.module.booking.dto.request.TicketRequest;
import com.eventra.backend.module.booking.dto.response.TicketResponse;
import com.eventra.backend.module.booking.service.TicketService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/events/{eventId}/tickets")
public class TicketController {

    private final TicketService ticketService;

    public TicketController(TicketService ticketService) {
        this.ticketService = ticketService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasRole('ORGANIZER')")
    public TicketResponse createTicket(
            @AuthenticationPrincipal AuthPrincipal principal,
            @PathVariable UUID eventId,
            @Valid @RequestBody TicketRequest request) {
        return ticketService.createTicket(principal.userId(), eventId, request);
    }

    @GetMapping
    public List<TicketResponse> getEventTickets(@PathVariable UUID eventId) {
        return ticketService.getEventTickets(eventId);
    }

    @GetMapping("/{ticketId}")
    public TicketResponse getTicket(
            @PathVariable UUID eventId,
            @PathVariable UUID ticketId) {
        return ticketService.getTicket(ticketId);
    }
}
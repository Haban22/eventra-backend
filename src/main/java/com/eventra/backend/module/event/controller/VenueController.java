package com.eventra.backend.module.event.controller;

import com.eventra.backend.module.event.dto.request.VenueRequest;
import com.eventra.backend.module.event.dto.response.VenueResponse;
import com.eventra.backend.module.event.service.VenueService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/venues")
public class VenueController {

    private final VenueService venueService;

    public VenueController(VenueService venueService) {
        this.venueService = venueService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasRole('ADMIN')")
    public VenueResponse createVenue(
            @Valid @RequestBody VenueRequest request) {
        return venueService.createVenue(request);
    }

    @GetMapping("/{id}")
    public VenueResponse getVenue(@PathVariable UUID id) {
        return venueService.getVenue(id);
    }

    @GetMapping("/search")
    public List<VenueResponse> searchByLocation(
            @RequestParam String city) {
        return venueService.searchByLocation(city);
    }
}
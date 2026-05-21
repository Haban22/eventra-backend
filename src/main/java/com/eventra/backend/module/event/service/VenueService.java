package com.eventra.backend.module.event.service;

import com.eventra.backend.module.auth.exception.ApiException;
import com.eventra.backend.module.event.dto.request.VenueRequest;
import com.eventra.backend.module.event.dto.response.VenueResponse;
import com.eventra.backend.module.event.entity.Venue;
import com.eventra.backend.module.event.repository.VenueRepository;
import com.eventra.backend.module.event.valueobject.Location;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
public class VenueService {

    private final VenueRepository venueRepository;

    public VenueService(VenueRepository venueRepository) {
        this.venueRepository = venueRepository;
    }

    @Transactional
    public VenueResponse createVenue(VenueRequest request) {
        Venue venue = new Venue();
        venue.setName(request.name().trim());
        venue.setMaxCapacity(request.maxCapacity());
        venue.setAmenities(request.amenities() != null ? request.amenities() : new ArrayList<>());

        Location location = new Location();
        location.setAddress(request.address());
        location.setCity(request.city());
        if (request.latitude() != null) location.setLatitude(request.latitude());
        if (request.longitude() != null) location.setLongitude(request.longitude());
        venue.setLocation(location);

        return VenueResponse.from(venueRepository.save(venue));
    }

    @Transactional(readOnly = true)
    public VenueResponse getVenue(UUID venueId) {
        return venueRepository.findById(venueId)
                .map(VenueResponse::from)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND,
                        "VENUE_NOT_FOUND", "Venue not found"));
    }

    @Transactional(readOnly = true)
    public List<VenueResponse> searchByLocation(String city) {
        return venueRepository.findByCity(city)
                .stream()
                .map(VenueResponse::from)
                .toList();
    }
}
package com.eventra.backend.module.event.service;

import com.eventra.backend.module.auth.exception.ApiException;
import com.eventra.backend.module.event.dto.request.EventRequest;
import com.eventra.backend.module.event.dto.request.EventSearchRequest;
import com.eventra.backend.module.event.dto.response.EventResponse;
import com.eventra.backend.module.event.dto.response.EventSummaryResponse;
import com.eventra.backend.module.event.entity.Event;
import com.eventra.backend.module.event.enums.EventStatus;
import com.eventra.backend.module.event.repository.CategoryRepository;
import com.eventra.backend.module.event.repository.EventApprovalRepository;
import com.eventra.backend.module.event.repository.EventRepository;
import com.eventra.backend.module.event.valueobject.Capacity;
import com.eventra.backend.module.event.valueobject.Location;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
public class EventService {

    private final EventRepository eventRepository;
    private final CategoryRepository categoryRepository;

    private final EventApprovalRepository approvalRepository;

    public EventService(EventRepository eventRepository,
                        CategoryRepository categoryRepository,
                        EventApprovalRepository approvalRepository) {
        this.eventRepository = eventRepository;
        this.categoryRepository = categoryRepository;
        this.approvalRepository = approvalRepository;
    }

    @Transactional
    public EventResponse createEvent(UUID organizerId, EventRequest request) {
        validateEventLocation(request);
        var category = categoryRepository.findById(request.categoryId())
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND,
                        "CATEGORY_NOT_FOUND", "Category not found"));

        Event event = new Event();
        event.setOrganizerId(organizerId);
        event.setTitle(request.title());
        event.setDescription(request.description());
        event.setDateTime(request.dateTime());
        event.setCategory(category);
        event.setVenueId(request.venueId());
        event.setOnline(request.isOnline());
        event.setOnlineUrl(request.onlineUrl());
        event.setCoverImageUrl(request.coverImageUrl());
        event.setStatus(EventStatus.DRAFT);

        Location location = new Location();
        location.setAddress(request.locationAddress());
        location.setCity(request.locationCity());
        if (request.locationLatitude() != null)
            location.setLatitude(request.locationLatitude());
        if (request.locationLongitude() != null)
            location.setLongitude(request.locationLongitude());
        event.setLocation(location);

        Capacity capacity = new Capacity();
        capacity.setMaximum(request.capacityMaximum());
        capacity.setReserved(0);
        event.setCapacity(capacity);
        event.setTags(request.tags() != null ? request.tags() : new ArrayList<>());

        return EventResponse.from(eventRepository.save(event));
    }

    @Transactional(readOnly = true)
    public EventResponse getEvent(UUID eventId) {
        return EventResponse.from(findEventOrThrow(eventId));
    }

    @Transactional
    public EventResponse updateEvent(UUID organizerId, UUID eventId, EventRequest request) {
        validateEventLocation(request);
        Event event = findEventOrThrow(eventId);
        assertOwner(event, organizerId);
        assertEditable(event);

        var category = categoryRepository.findById(request.categoryId())
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND,
                        "CATEGORY_NOT_FOUND", "Category not found"));

        event.setTitle(request.title());
        event.setDescription(request.description());
        event.setDateTime(request.dateTime());
        event.setCategory(category);
        event.setVenueId(request.venueId());
        event.setOnline(request.isOnline());
        event.setOnlineUrl(request.onlineUrl());
        event.setCoverImageUrl(request.coverImageUrl());

        Location location = new Location();
        location.setAddress(request.locationAddress());
        location.setCity(request.locationCity());
        if (request.locationLatitude() != null)
            location.setLatitude(request.locationLatitude());
        if (request.locationLongitude() != null)
            location.setLongitude(request.locationLongitude());
        event.setLocation(location);

        Capacity capacity = new Capacity();
        capacity.setMaximum(request.capacityMaximum());
        capacity.setReserved(event.getCapacity() != null ? event.getCapacity().getReserved() : 0);
        event.setCapacity(capacity);
        event.setTags(request.tags() != null ? request.tags() : new ArrayList<>());

        return EventResponse.from(eventRepository.save(event));
    }

    @Transactional
    public EventResponse publishEvent(UUID organizerId, UUID eventId) {
        Event event = findEventOrThrow(eventId);
        assertOwner(event, organizerId);
        assertEditable(event);
        event.publish();
        return EventResponse.from(eventRepository.save(event));
    }

    @Transactional
    public EventResponse duplicateEvent(UUID organizerId, UUID eventId) {
        Event original = findEventOrThrow(eventId);
        assertOwner(original, organizerId);

        Event copy = new Event();
        copy.setOrganizerId(organizerId);
        copy.setTitle(original.getTitle() + " (Copy)");
        copy.setDescription(original.getDescription());
        
        java.time.Instant copyDateTime = original.getDateTime();
        if (copyDateTime.isBefore(java.time.Instant.now())) {
            copyDateTime = java.time.Instant.now().plus(java.time.Duration.ofDays(7));
        }
        copy.setDateTime(copyDateTime);
        
        copy.setLocation(original.getLocation());
        copy.setVenueId(original.getVenueId());
        copy.setCategory(original.getCategory());
        copy.setOnline(original.isOnline());
        copy.setOnlineUrl(original.getOnlineUrl());
        copy.setCoverImageUrl(original.getCoverImageUrl());
        copy.setStatus(EventStatus.DRAFT);

        Capacity capacity = new Capacity();
        capacity.setMaximum(original.getCapacity() != null ? original.getCapacity().getMaximum() : 0);
        capacity.setReserved(0);
        copy.setCapacity(capacity);

        return EventResponse.from(eventRepository.save(copy));
    }

    @Transactional
    public void cancelEvent(UUID organizerId, UUID eventId) {
        Event event = findEventOrThrow(eventId);
        assertOwner(event, organizerId);
        if (event.getStatus() == EventStatus.CANCELLED) {
            throw new ApiException(HttpStatus.BAD_REQUEST,
                    "ALREADY_CANCELLED", "Event is already cancelled");
        }
        event.cancel();
        eventRepository.save(event);
    }

    @Transactional(readOnly = true)
    public List<EventResponse> getOrganizerEvents(UUID organizerId) {
        return eventRepository.findByOrganizerId(organizerId)
                .stream()
                .map(event -> {
                    if (event.getStatus() == EventStatus.DRAFT) {
                        return approvalRepository.findByEventId(event.getId())
                                .map(approval -> EventResponse.from(event, approval.getFeedback()))
                                .orElseGet(() -> EventResponse.from(event));
                    }
                    return EventResponse.from(event);
                })
                .toList();
    }
    @Transactional(readOnly = true)
    public Page<EventSummaryResponse> searchEvents(EventSearchRequest request, int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("dateTime").ascending());
        return eventRepository.searchEvents(
                        request.categoryId(),
                        request.city(),
                        request.keyword(),
                        request.from(),
                        request.to(),
                        pageable
                )
                .map(EventSummaryResponse::from);
    }

    // ── Helpers ──────────────────────────────────────────────────────────

    private Event findEventOrThrow(UUID eventId) {
        return eventRepository.findById(eventId)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND,
                        "EVENT_NOT_FOUND", "Event not found"));
    }

    private void assertOwner(Event event, UUID organizerId) {
        if (!event.getOrganizerId().equals(organizerId)) {
            throw new ApiException(HttpStatus.FORBIDDEN,
                    "NOT_EVENT_OWNER", "You do not own this event");
        }
    }

    private void assertEditable(Event event) {
        if (event.getStatus() == EventStatus.PUBLISHED ||
                event.getStatus() == EventStatus.CANCELLED) {
            throw new ApiException(HttpStatus.BAD_REQUEST,
                    "EVENT_NOT_EDITABLE", "Event cannot be edited in its current status");
        }
    }

    private void validateEventLocation(EventRequest request) {
        if (request.isOnline()) {
            if (request.onlineUrl() == null || request.onlineUrl().isBlank()) {
                throw new ApiException(HttpStatus.BAD_REQUEST, "INVALID_ONLINE_URL", "Online URL is required for online events");
            }
        } else {
            if (request.locationAddress() == null || request.locationAddress().isBlank() ||
                request.locationCity() == null || request.locationCity().isBlank()) {
                throw new ApiException(HttpStatus.BAD_REQUEST, "INVALID_PHYSICAL_LOCATION", "Physical location city and address are required for in-person events");
            }
        }
    }
}
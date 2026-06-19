package com.eventra.backend.module.event;

import com.eventra.backend.module.auth.exception.ApiException;
import com.eventra.backend.module.event.dto.request.EventRequest;
import com.eventra.backend.module.event.dto.response.EventResponse;
import com.eventra.backend.module.event.entity.Category;
import com.eventra.backend.module.event.entity.Event;
import com.eventra.backend.module.event.enums.EventStatus;
import com.eventra.backend.module.event.repository.CategoryRepository;
import com.eventra.backend.module.event.repository.EventApprovalRepository;
import com.eventra.backend.module.event.repository.EventRepository;
import com.eventra.backend.module.event.service.EventService;
import com.eventra.backend.module.event.valueobject.Capacity;
import com.eventra.backend.module.event.valueobject.Location;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class EventServiceTest {

    @Mock
    private EventRepository eventRepository;

    @Mock
    private CategoryRepository categoryRepository;

    @Mock
    private EventApprovalRepository approvalRepository;

    @InjectMocks
    private EventService eventService;

    private EventRequest createValidPhysicalRequest(UUID categoryId) {
        return new EventRequest(
                "Tech Conference",
                "Description",
                Instant.now().plusSeconds(86400),
                "123 Street",
                "Amman",
                31.95,
                35.91,
                null,
                categoryId,
                100,
                false,
                null,
                "https://cover.jpg",
                List.of("tech", "dev")
        );
    }

    private EventRequest createValidOnlineRequest(UUID categoryId) {
        return new EventRequest(
                "Tech Conference",
                "Description",
                Instant.now().plusSeconds(86400),
                null,
                null,
                null,
                null,
                null,
                categoryId,
                100,
                true,
                "https://online.url/webinar",
                "https://cover.jpg",
                List.of("tech", "dev")
        );
    }

    @Test
    void createEvent_PhysicalEvent_Success() {
        UUID organizerId = UUID.randomUUID();
        UUID categoryId = UUID.randomUUID();
        EventRequest request = createValidPhysicalRequest(categoryId);

        Category category = new Category();
        category.setId(categoryId);
        category.setName("Technology");

        when(categoryRepository.findById(categoryId)).thenReturn(Optional.of(category));
        when(eventRepository.save(any(Event.class))).thenAnswer(invocation -> {
            Event e = invocation.getArgument(0);
            e.setId(UUID.randomUUID());
            return e;
        });

        EventResponse response = eventService.createEvent(organizerId, request);

        assertNotNull(response);
        assertNotNull(response.id());
        assertEquals("Tech Conference", response.title());
        assertEquals("Amman", response.locationCity());
        assertEquals(EventStatus.DRAFT, response.status());
        verify(eventRepository).save(any(Event.class));
    }

    @Test
    void createEvent_OnlineEvent_Success() {
        UUID organizerId = UUID.randomUUID();
        UUID categoryId = UUID.randomUUID();
        EventRequest request = createValidOnlineRequest(categoryId);

        Category category = new Category();
        category.setId(categoryId);
        category.setName("Technology");

        when(categoryRepository.findById(categoryId)).thenReturn(Optional.of(category));
        when(eventRepository.save(any(Event.class))).thenAnswer(invocation -> {
            Event e = invocation.getArgument(0);
            e.setId(UUID.randomUUID());
            return e;
        });

        EventResponse response = eventService.createEvent(organizerId, request);

        assertNotNull(response);
        assertNotNull(response.id());
        assertEquals("https://online.url/webinar", response.onlineUrl());
        assertTrue(response.isOnline());
        verify(eventRepository).save(any(Event.class));
    }

    @Test
    void createEvent_CategoryNotFound_ThrowsNotFound() {
        UUID organizerId = UUID.randomUUID();
        UUID categoryId = UUID.randomUUID();
        EventRequest request = createValidPhysicalRequest(categoryId);

        when(categoryRepository.findById(categoryId)).thenReturn(Optional.empty());

        ApiException exception = assertThrows(ApiException.class, () ->
                eventService.createEvent(organizerId, request));

        assertEquals(HttpStatus.NOT_FOUND, exception.getStatus());
        assertEquals("CATEGORY_NOT_FOUND", exception.getError());
    }

    @Test
    void createEvent_ValidationFails_OnlineEventWithoutUrl() {
        UUID organizerId = UUID.randomUUID();
        UUID categoryId = UUID.randomUUID();
        
        // Online event with empty URL
        EventRequest request = new EventRequest(
                "Online Meetup", "Desc", Instant.now().plusSeconds(3600),
                null, null, null, null, null, categoryId, 50, true, "", null, null
        );

        ApiException exception = assertThrows(ApiException.class, () ->
                eventService.createEvent(organizerId, request));

        assertEquals(HttpStatus.BAD_REQUEST, exception.getStatus());
        assertEquals("INVALID_ONLINE_URL", exception.getError());
    }

    @Test
    void createEvent_ValidationFails_PhysicalEventWithoutAddress() {
        UUID organizerId = UUID.randomUUID();
        UUID categoryId = UUID.randomUUID();
        
        // Physical event with missing address
        EventRequest request = new EventRequest(
                "Offline Meetup", "Desc", Instant.now().plusSeconds(3600),
                "", "Amman", null, null, null, categoryId, 50, false, null, null, null
        );

        ApiException exception = assertThrows(ApiException.class, () ->
                eventService.createEvent(organizerId, request));

        assertEquals(HttpStatus.BAD_REQUEST, exception.getStatus());
        assertEquals("INVALID_PHYSICAL_LOCATION", exception.getError());
    }

    @Test
    void updateEvent_Success() {
        UUID organizerId = UUID.randomUUID();
        UUID eventId = UUID.randomUUID();
        UUID categoryId = UUID.randomUUID();
        EventRequest request = createValidPhysicalRequest(categoryId);

        Category category = new Category();
        category.setId(categoryId);

        Event existingEvent = new Event();
        existingEvent.setId(eventId);
        existingEvent.setOrganizerId(organizerId);
        existingEvent.setStatus(EventStatus.DRAFT);

        when(eventRepository.findById(eventId)).thenReturn(Optional.of(existingEvent));
        when(categoryRepository.findById(categoryId)).thenReturn(Optional.of(category));
        when(eventRepository.save(any(Event.class))).thenAnswer(invocation -> invocation.getArgument(0));

        EventResponse response = eventService.updateEvent(organizerId, eventId, request);

        assertNotNull(response);
        assertEquals("Tech Conference", response.title());
        verify(eventRepository).save(existingEvent);
    }

    @Test
    void updateEvent_NotOwner_ThrowsForbidden() {
        UUID organizerId = UUID.randomUUID();
        UUID otherOrganizerId = UUID.randomUUID();
        UUID eventId = UUID.randomUUID();
        UUID categoryId = UUID.randomUUID();
        EventRequest request = createValidPhysicalRequest(categoryId);

        Event existingEvent = new Event();
        existingEvent.setId(eventId);
        existingEvent.setOrganizerId(otherOrganizerId); // Different owner
        existingEvent.setStatus(EventStatus.DRAFT);

        when(eventRepository.findById(eventId)).thenReturn(Optional.of(existingEvent));

        ApiException exception = assertThrows(ApiException.class, () ->
                eventService.updateEvent(organizerId, eventId, request));

        assertEquals(HttpStatus.FORBIDDEN, exception.getStatus());
        assertEquals("NOT_EVENT_OWNER", exception.getError());
    }

    @Test
    void updateEvent_NotEditable_ThrowsBadRequest() {
        UUID organizerId = UUID.randomUUID();
        UUID eventId = UUID.randomUUID();
        UUID categoryId = UUID.randomUUID();
        EventRequest request = createValidPhysicalRequest(categoryId);

        Event existingEvent = new Event();
        existingEvent.setId(eventId);
        existingEvent.setOrganizerId(organizerId);
        existingEvent.setStatus(EventStatus.PUBLISHED); // Not editable

        when(eventRepository.findById(eventId)).thenReturn(Optional.of(existingEvent));

        ApiException exception = assertThrows(ApiException.class, () ->
                eventService.updateEvent(organizerId, eventId, request));

        assertEquals(HttpStatus.BAD_REQUEST, exception.getStatus());
        assertEquals("EVENT_NOT_EDITABLE", exception.getError());
    }

    @Test
    void publishEvent_Success() {
        UUID organizerId = UUID.randomUUID();
        UUID eventId = UUID.randomUUID();

        Event existingEvent = new Event();
        existingEvent.setId(eventId);
        existingEvent.setOrganizerId(organizerId);
        existingEvent.setStatus(EventStatus.DRAFT);

        when(eventRepository.findById(eventId)).thenReturn(Optional.of(existingEvent));
        when(eventRepository.save(any(Event.class))).thenAnswer(invocation -> invocation.getArgument(0));

        EventResponse response = eventService.publishEvent(organizerId, eventId);

        assertNotNull(response);
        assertEquals(EventStatus.PENDING_APPROVAL, response.status());
        verify(eventRepository).save(existingEvent);
    }

    @Test
    void duplicateEvent_FutureDate_KeepsDate() {
        UUID organizerId = UUID.randomUUID();
        UUID eventId = UUID.randomUUID();
        Instant futureDate = Instant.now().plusSeconds(86400);

        Category category = new Category();
        category.setId(UUID.randomUUID());

        Location location = new Location();
        location.setCity("Amman");

        Capacity capacity = new Capacity();
        capacity.setMaximum(100);

        Event originalEvent = new Event();
        originalEvent.setId(eventId);
        originalEvent.setOrganizerId(organizerId);
        originalEvent.setTitle("Original Event");
        originalEvent.setDateTime(futureDate);
        originalEvent.setLocation(location);
        originalEvent.setCategory(category);
        originalEvent.setCapacity(capacity);

        when(eventRepository.findById(eventId)).thenReturn(Optional.of(originalEvent));
        when(eventRepository.save(any(Event.class))).thenAnswer(invocation -> {
            Event e = invocation.getArgument(0);
            e.setId(UUID.randomUUID());
            return e;
        });

        EventResponse response = eventService.duplicateEvent(organizerId, eventId);

        assertNotNull(response);
        assertEquals("Original Event (Copy)", response.title());
        assertEquals(futureDate, response.dateTime());
        assertEquals(EventStatus.DRAFT, response.status());
    }

    @Test
    void duplicateEvent_PastDate_SetsToFutureDate() {
        UUID organizerId = UUID.randomUUID();
        UUID eventId = UUID.randomUUID();
        Instant pastDate = Instant.now().minusSeconds(86400); // 1 day in past

        Category category = new Category();
        category.setId(UUID.randomUUID());

        Location location = new Location();
        location.setCity("Amman");

        Capacity capacity = new Capacity();
        capacity.setMaximum(100);

        Event originalEvent = new Event();
        originalEvent.setId(eventId);
        originalEvent.setOrganizerId(organizerId);
        originalEvent.setTitle("Original Event");
        originalEvent.setDateTime(pastDate);
        originalEvent.setLocation(location);
        originalEvent.setCategory(category);
        originalEvent.setCapacity(capacity);

        when(eventRepository.findById(eventId)).thenReturn(Optional.of(originalEvent));
        when(eventRepository.save(any(Event.class))).thenAnswer(invocation -> {
            Event e = invocation.getArgument(0);
            e.setId(UUID.randomUUID());
            return e;
        });

        EventResponse response = eventService.duplicateEvent(organizerId, eventId);

        assertNotNull(response);
        assertEquals("Original Event (Copy)", response.title());
        assertTrue(response.dateTime().isAfter(Instant.now())); // Check it was shifted to future!
        assertEquals(EventStatus.DRAFT, response.status());
    }

    @Test
    void cancelEvent_Success() {
        UUID organizerId = UUID.randomUUID();
        UUID eventId = UUID.randomUUID();

        Event existingEvent = new Event();
        existingEvent.setId(eventId);
        existingEvent.setOrganizerId(organizerId);
        existingEvent.setStatus(EventStatus.PUBLISHED);

        when(eventRepository.findById(eventId)).thenReturn(Optional.of(existingEvent));

        eventService.cancelEvent(organizerId, eventId);

        assertEquals(EventStatus.CANCELLED, existingEvent.getStatus());
        verify(eventRepository).save(existingEvent);
    }

    @Test
    void cancelEvent_AlreadyCancelled_ThrowsBadRequest() {
        UUID organizerId = UUID.randomUUID();
        UUID eventId = UUID.randomUUID();

        Event existingEvent = new Event();
        existingEvent.setId(eventId);
        existingEvent.setOrganizerId(organizerId);
        existingEvent.setStatus(EventStatus.CANCELLED); // Already cancelled

        when(eventRepository.findById(eventId)).thenReturn(Optional.of(existingEvent));

        ApiException exception = assertThrows(ApiException.class, () ->
                eventService.cancelEvent(organizerId, eventId));

        assertEquals(HttpStatus.BAD_REQUEST, exception.getStatus());
        assertEquals("ALREADY_CANCELLED", exception.getError());
    }
}

package com.eventra.backend.module.event.service;

import com.eventra.backend.module.notification.service.NotificationEventService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Event service with notification hooks for event lifecycle changes.
 */
@Service
@RequiredArgsConstructor
@Transactional
public class EventService {

    private final NotificationEventService notificationEventService;

    /**
     * Updates an event and notifies all affected attendees.
     *
     * @param eventId         updated event ID
     * @param eventTitle      updated event title
     * @param attendeeUserIds IDs of users with bookings for the event
     */
    public void updateEvent(Long eventId, String eventTitle, List<Long> attendeeUserIds) {
        // TODO: persist event update when Event entity is implemented
        notificationEventService.onEventUpdated(attendeeUserIds, eventTitle, eventId);
    }
}

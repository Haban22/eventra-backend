package com.eventra.backend.module.booking.service;

import com.eventra.backend.module.notification.service.NotificationEventService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Booking service with notification hooks for booking lifecycle events.
 */
@Service
@RequiredArgsConstructor
@Transactional
public class BookingService {

    private final NotificationEventService notificationEventService;

    /**
     * Confirms a booking and notifies the user.
     *
     * @param userId     booking owner user ID
     * @param bookingId  confirmed booking ID
     * @param eventTitle booked event title
     */
    public void confirmBooking(Long userId, Long bookingId, String eventTitle) {
        // TODO: persist booking confirmation when Booking entity is implemented
        notificationEventService.onBookingConfirmed(userId, eventTitle, bookingId);
    }

    /**
     * Cancels a booking and notifies the user.
     *
     * @param userId     booking owner user ID
     * @param bookingId  cancelled booking ID
     * @param eventTitle cancelled event title
     */
    public void cancelBooking(Long userId, Long bookingId, String eventTitle) {
        // TODO: persist booking cancellation when Booking entity is implemented
        notificationEventService.onBookingCancelled(userId, eventTitle, bookingId);
    }
}

package com.eventra.backend.module.notification.service;

import com.eventra.backend.module.notification.dto.NotificationRequest;
import com.eventra.backend.module.notification.dto.NotificationResponse;
import com.eventra.backend.module.notification.enums.NotificationType;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Handles domain-event-driven notification creation across booking, event, and community flows.
 */
@Service
@RequiredArgsConstructor
@Transactional
public class NotificationEventService {

    private final NotificationService notificationService;

    /**
     * Creates a notification when a user successfully books an event.
     *
     * @param userId     ID of the user who booked
     * @param eventTitle title of the booked event
     * @param bookingId  ID of the confirmed booking
     * @return created notification response
     */
    public NotificationResponse onBookingConfirmed(Long userId, String eventTitle, Long bookingId) {
        return notificationService.createNotification(NotificationRequest.builder()
                .userId(userId)
                .type(NotificationType.BOOKING_CONFIRMED)
                .title("Booking Confirmed")
                .message(String.format("Your booking for \"%s\" has been confirmed. Booking ID: %d.", eventTitle, bookingId))
                .build());
    }

    /**
     * Creates a notification when a user cancels a booking.
     *
     * @param userId     ID of the user who cancelled
     * @param eventTitle title of the cancelled event
     * @param bookingId  ID of the cancelled booking
     * @return created notification response
     */
    public NotificationResponse onBookingCancelled(Long userId, String eventTitle, Long bookingId) {
        return notificationService.createNotification(NotificationRequest.builder()
                .userId(userId)
                .type(NotificationType.BOOKING_CANCELLED)
                .title("Booking Cancelled")
                .message(String.format("Your booking for \"%s\" has been cancelled. Booking ID: %d.", eventTitle, bookingId))
                .build());
    }

    /**
     * Notifies all affected users when an admin updates an event.
     *
     * @param attendeeUserIds IDs of users with bookings for the event
     * @param eventTitle      updated event title
     * @param eventId         updated event ID
     */
    public void onEventUpdated(List<Long> attendeeUserIds, String eventTitle, Long eventId) {
        attendeeUserIds.forEach(userId -> notificationService.createNotification(NotificationRequest.builder()
                .userId(userId)
                .type(NotificationType.EVENT_UPDATED)
                .title("Event Updated")
                .message(String.format("The event \"%s\" has been updated. Please review the latest details. Event ID: %d.",
                        eventTitle, eventId))
                .build()));
    }

    /**
     * Creates a notification when a user joins a community.
     *
     * @param userId        ID of the user who joined
     * @param communityName name of the community
     * @param communityId   ID of the community
     * @return created notification response
     */
    public NotificationResponse onCommunityJoined(Long userId, String communityName, Long communityId) {
        return notificationService.createNotification(NotificationRequest.builder()
                .userId(userId)
                .type(NotificationType.COMMUNITY_UPDATE)
                .title("Welcome to the Community")
                .message(String.format("You have successfully joined \"%s\". Community ID: %d.", communityName, communityId))
                .build());
    }

    /**
     * Sends a community announcement notification to all community members.
     *
     * @param memberUserIds IDs of community members to notify
     * @param communityName name of the community
     * @param announcement  announcement message from admin
     */
    public void onCommunityAnnouncement(List<Long> memberUserIds, String communityName, String announcement) {
        memberUserIds.forEach(userId -> notificationService.createNotification(NotificationRequest.builder()
                .userId(userId)
                .type(NotificationType.COMMUNITY_UPDATE)
                .title(String.format("Announcement: %s", communityName))
                .message(announcement)
                .build()));
    }

    /**
     * Sends a community invitation notification to a user.
     *
     * @param userId        invited user ID
     * @param communityName community name
     * @param communityId   community ID
     * @return created notification response
     */
    public NotificationResponse onCommunityInvitation(Long userId, String communityName, Long communityId) {
        return notificationService.createNotification(NotificationRequest.builder()
                .userId(userId)
                .type(NotificationType.COMMUNITY_INVITATION)
                .title("Community Invitation")
                .message(String.format("You have been invited to join \"%s\". Community ID: %d.", communityName, communityId))
                .build());
    }
}

package com.eventra.backend.module.community.service;

import com.eventra.backend.module.notification.service.NotificationEventService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Community service with notification hooks for membership and announcements.
 */
@Service
@RequiredArgsConstructor
@Transactional
public class CommunityService {

    private final NotificationEventService notificationEventService;

    /**
     * Handles a user joining a community and notifies the user.
     *
     * @param userId        joining user ID
     * @param communityId   community ID
     * @param communityName community display name
     */
    public void joinCommunity(Long userId, Long communityId, String communityName) {
        // TODO: persist community membership when Community entity is implemented
        notificationEventService.onCommunityJoined(userId, communityName, communityId);
    }

    /**
     * Sends an admin announcement to all community members.
     *
     * @param communityId   community ID
     * @param communityName community display name
     * @param memberUserIds IDs of community members
     * @param announcement  announcement message
     */
    public void sendAnnouncement(Long communityId, String communityName, List<Long> memberUserIds, String announcement) {
        // TODO: validate admin permissions when Community entity is implemented
        notificationEventService.onCommunityAnnouncement(memberUserIds, communityName, announcement);
    }
}

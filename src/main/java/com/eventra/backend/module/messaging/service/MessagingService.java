package com.eventra.backend.module.messaging.service;

import com.eventra.backend.module.auth.entity.User;
import com.eventra.backend.module.auth.entity.UserRole;
import com.eventra.backend.module.auth.exception.ApiException;
import com.eventra.backend.module.auth.repository.UserRepository;
import com.eventra.backend.module.messaging.dto.*;
import com.eventra.backend.module.messaging.entity.BroadcastMessage;
import com.eventra.backend.module.messaging.entity.CommunityMessage;
import com.eventra.backend.module.messaging.entity.DirectMessage;
import com.eventra.backend.module.messaging.entity.EventMessage;
import com.eventra.backend.module.messaging.enums.BroadcastTargetRole;
import com.eventra.backend.module.messaging.repository.BroadcastMessageRepository;
import com.eventra.backend.module.messaging.repository.CommunityMessageRepository;
import com.eventra.backend.module.messaging.repository.DirectMessageRepository;
import com.eventra.backend.module.messaging.repository.EventMessageRepository;
import org.springframework.http.HttpStatus;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class MessagingService {
    private final DirectMessageRepository directMessageRepository;
    private final EventMessageRepository eventMessageRepository;
    private final CommunityMessageRepository communityMessageRepository;
    private final BroadcastMessageRepository broadcastMessageRepository;
    private final UserRepository userRepository;
    private final SimpMessagingTemplate messagingTemplate;

    public MessagingService(DirectMessageRepository directMessageRepository,
                            EventMessageRepository eventMessageRepository,
                            CommunityMessageRepository communityMessageRepository,
                            BroadcastMessageRepository broadcastMessageRepository,
                            UserRepository userRepository,
                            SimpMessagingTemplate messagingTemplate) {
        this.directMessageRepository = directMessageRepository;
        this.eventMessageRepository = eventMessageRepository;
        this.communityMessageRepository = communityMessageRepository;
        this.broadcastMessageRepository = broadcastMessageRepository;
        this.userRepository = userRepository;
        this.messagingTemplate = messagingTemplate;
    }

    // ── Direct messages ─────────────────────────────────────────────────────

    @Transactional
    public DirectMessageResponse sendDirectMessage(UUID senderId, SendDirectMessageRequest request) {
        if (!userRepository.existsById(request.receiverId())) {
            throw new ApiException(HttpStatus.NOT_FOUND, "NOT_FOUND", "Recipient not found");
        }
        DirectMessage message = new DirectMessage();
        message.setSenderId(senderId);
        message.setReceiverId(request.receiverId());
        message.setContent(request.content());
        message = directMessageRepository.save(message);

        Map<UUID, User> users = resolveUsers(Set.of(senderId, request.receiverId()));
        DirectMessageResponse response = toDirectMessageResponse(message, users);
        messagingTemplate.convertAndSendToUser(request.receiverId().toString(), "/queue/messages", response);
        return response;
    }

    @Transactional(readOnly = true)
    public List<DirectMessageResponse> getConversation(UUID me, UUID otherUserId) {
        List<DirectMessage> messages = directMessageRepository.findConversation(me, otherUserId);
        Map<UUID, User> users = resolveUsers(Set.of(me, otherUserId));
        return messages.stream().map(m -> toDirectMessageResponse(m, users)).toList();
    }

    @Transactional(readOnly = true)
    public List<DMThreadResponse> getMyThreads(UUID me) {
        List<DirectMessage> all = directMessageRepository.findAllForUser(me);
        Set<UUID> partnerIds = all.stream()
                .map(m -> m.getSenderId().equals(me) ? m.getReceiverId() : m.getSenderId())
                .collect(Collectors.toSet());
        Map<UUID, User> users = resolveUsers(partnerIds);

        Map<UUID, List<DirectMessage>> byPartner = all.stream()
                .collect(Collectors.groupingBy(m -> m.getSenderId().equals(me) ? m.getReceiverId() : m.getSenderId()));

        return byPartner.entrySet().stream().map(entry -> {
            UUID partnerId = entry.getKey();
            List<DirectMessage> thread = entry.getValue();
            DirectMessage last = thread.stream().max(Comparator.comparing(DirectMessage::getCreatedAt)).orElseThrow();
            long unread = thread.stream().filter(m -> !m.isRead() && m.getReceiverId().equals(me)).count();
            User partner = users.get(partnerId);
            return new DMThreadResponse(
                    partnerId,
                    partner != null ? partner.getFullName() : "Unknown User",
                    partner != null ? partner.getProfilePictureUrl() : null,
                    partner != null ? partner.getRole().name() : null,
                    last.getContent(),
                    last.getCreatedAt(),
                    unread
            );
        }).sorted(Comparator.comparing(DMThreadResponse::lastMessageAt).reversed()).toList();
    }

    @Transactional
    public void markConversationRead(UUID me, UUID otherUserId) {
        directMessageRepository.markConversationRead(me, otherUserId);
    }

    // ── Event chat ───────────────────────────────────────────────────────────

    @Transactional
    public EventMessageResponse sendEventMessage(UUID userId, UUID eventId, SendMessageContentRequest request) {
        EventMessage message = new EventMessage();
        message.setEventId(eventId);
        message.setUserId(userId);
        message.setContent(request.content());
        message = eventMessageRepository.save(message);

        Map<UUID, User> users = resolveUsers(Set.of(userId));
        EventMessageResponse response = toEventMessageResponse(message, users);
        messagingTemplate.convertAndSend("/topic/events/" + eventId + "/chat", response);
        return response;
    }

    @Transactional(readOnly = true)
    public List<EventMessageResponse> getEventMessages(UUID eventId) {
        List<EventMessage> messages = eventMessageRepository.findByEventIdOrderByCreatedAtAsc(eventId);
        Map<UUID, User> users = resolveUsers(messages.stream().map(EventMessage::getUserId).collect(Collectors.toSet()));
        return messages.stream().map(m -> toEventMessageResponse(m, users)).toList();
    }

    @Transactional
    public void deleteEventMessage(UUID requesterId, UserRole requesterRole, UUID eventId, UUID messageId) {
        EventMessage message = eventMessageRepository.findById(messageId)
                .filter(m -> m.getEventId().equals(eventId))
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "NOT_FOUND", "Message not found"));
        boolean isAuthor = message.getUserId().equals(requesterId);
        boolean isModerator = requesterRole == UserRole.ORGANIZER || requesterRole == UserRole.ADMIN;
        if (!isAuthor && !isModerator) {
            throw new ApiException(HttpStatus.FORBIDDEN, "FORBIDDEN", "You cannot delete this message");
        }
        eventMessageRepository.delete(message);
        messagingTemplate.convertAndSend("/topic/events/" + eventId + "/chat/deleted", messageId);
    }

    // ── Community chat ───────────────────────────────────────────────────────

    @Transactional
    public CommunityMessageResponse sendCommunityMessage(UUID userId, Long communityId, SendMessageContentRequest request) {
        CommunityMessage message = new CommunityMessage();
        message.setCommunityId(communityId);
        message.setUserId(userId);
        message.setContent(request.content());
        message = communityMessageRepository.save(message);

        Map<UUID, User> users = resolveUsers(Set.of(userId));
        CommunityMessageResponse response = toCommunityMessageResponse(message, users);
        messagingTemplate.convertAndSend("/topic/communities/" + communityId + "/chat", response);
        return response;
    }

    @Transactional(readOnly = true)
    public List<CommunityMessageResponse> getCommunityMessages(Long communityId) {
        List<CommunityMessage> messages = communityMessageRepository.findByCommunityIdOrderByCreatedAtAsc(communityId);
        Map<UUID, User> users = resolveUsers(messages.stream().map(CommunityMessage::getUserId).collect(Collectors.toSet()));
        return messages.stream().map(m -> toCommunityMessageResponse(m, users)).toList();
    }

    // ── Broadcasts ───────────────────────────────────────────────────────────

    @Transactional
    public BroadcastMessageResponse sendBroadcast(UUID senderId, UserRole senderRole, SendBroadcastRequest request) {
        boolean allowed = (senderRole == UserRole.ORGANIZER && request.targetRole() == BroadcastTargetRole.ATTENDEE)
                || (senderRole == UserRole.ADMIN && request.targetRole() == BroadcastTargetRole.ORGANIZER);
        if (!allowed) {
            throw new ApiException(HttpStatus.FORBIDDEN, "FORBIDDEN", "You cannot broadcast to this audience");
        }

        BroadcastMessage message = new BroadcastMessage();
        message.setSenderId(senderId);
        message.setSubject(request.subject());
        message.setContent(request.content());
        message.setTargetRole(request.targetRole());
        message = broadcastMessageRepository.save(message);

        Map<UUID, User> users = resolveUsers(Set.of(senderId));
        BroadcastMessageResponse response = toBroadcastResponse(message, users);
        // All clients of the target role subscribe to this shared topic on connect —
        // simpler than resolving individual user queues for a potentially large audience.
        messagingTemplate.convertAndSend("/topic/broadcasts/" + request.targetRole().name(), response);
        return response;
    }

    @Transactional(readOnly = true)
    public List<BroadcastMessageResponse> getMyBroadcasts(UUID me, UserRole myRole) {
        BroadcastTargetRole matchingTarget = myRole == UserRole.ATTENDEE ? BroadcastTargetRole.ATTENDEE
                : myRole == UserRole.ORGANIZER ? BroadcastTargetRole.ORGANIZER : null;
        List<BroadcastMessage> messages = matchingTarget != null
                ? broadcastMessageRepository.findBySenderIdOrTargetRoleOrderByCreatedAtDesc(me, matchingTarget)
                : broadcastMessageRepository.findBySenderIdOrTargetRoleOrderByCreatedAtDesc(me, BroadcastTargetRole.ATTENDEE)
                        .stream().filter(b -> b.getSenderId().equals(me)).toList();
        Map<UUID, User> users = resolveUsers(messages.stream().map(BroadcastMessage::getSenderId).collect(Collectors.toSet()));
        return messages.stream().map(m -> toBroadcastResponse(m, users)).toList();
    }

    // ── Helpers ──────────────────────────────────────────────────────────────

    private Map<UUID, User> resolveUsers(Set<UUID> ids) {
        return userRepository.findAllById(ids).stream().collect(Collectors.toMap(User::getId, u -> u));
    }

    private DirectMessageResponse toDirectMessageResponse(DirectMessage m, Map<UUID, User> users) {
        User sender = users.get(m.getSenderId());
        User receiver = users.get(m.getReceiverId());
        return new DirectMessageResponse(
                m.getId(), m.getSenderId(), sender != null ? sender.getFullName() : "Unknown User",
                sender != null ? sender.getProfilePictureUrl() : null, sender != null ? sender.getRole().name() : null,
                m.getReceiverId(), receiver != null ? receiver.getFullName() : "Unknown User",
                receiver != null ? receiver.getRole().name() : null,
                m.getContent(), m.getCreatedAt(), m.isRead()
        );
    }

    private EventMessageResponse toEventMessageResponse(EventMessage m, Map<UUID, User> users) {
        User user = users.get(m.getUserId());
        return new EventMessageResponse(
                m.getId(), m.getEventId(), m.getUserId(), user != null ? user.getFullName() : "Unknown User",
                user != null ? user.getProfilePictureUrl() : null, user != null ? user.getRole().name() : null,
                m.getContent(), m.getCreatedAt()
        );
    }

    private CommunityMessageResponse toCommunityMessageResponse(CommunityMessage m, Map<UUID, User> users) {
        User user = users.get(m.getUserId());
        return new CommunityMessageResponse(
                m.getId(), m.getCommunityId(), m.getUserId(), user != null ? user.getFullName() : "Unknown User",
                user != null ? user.getProfilePictureUrl() : null, m.getContent(), m.getCreatedAt()
        );
    }

    private BroadcastMessageResponse toBroadcastResponse(BroadcastMessage m, Map<UUID, User> users) {
        User sender = users.get(m.getSenderId());
        long recipientCount = userRepository.countByRole(
                m.getTargetRole() == BroadcastTargetRole.ATTENDEE ? UserRole.ATTENDEE : UserRole.ORGANIZER
        );
        return new BroadcastMessageResponse(
                m.getId(), m.getSenderId(), sender != null ? sender.getFullName() : "Unknown User",
                sender != null ? sender.getRole().name() : null, m.getTargetRole().name(),
                m.getSubject(), m.getContent(), m.getCreatedAt(), recipientCount
        );
    }
}

package com.eventra.backend.module.messaging.controller;

import com.eventra.backend.module.auth.security.AuthPrincipal;
import com.eventra.backend.module.messaging.dto.EventMessageResponse;
import com.eventra.backend.module.messaging.dto.SendMessageContentRequest;
import com.eventra.backend.module.messaging.service.MessagingService;
import jakarta.validation.Valid;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/events/{eventId}/messages")
public class EventChatController {
    private final MessagingService messagingService;

    public EventChatController(MessagingService messagingService) {
        this.messagingService = messagingService;
    }

    @PostMapping
    public EventMessageResponse send(@AuthenticationPrincipal AuthPrincipal principal, @PathVariable UUID eventId, @Valid @RequestBody SendMessageContentRequest request) {
        return messagingService.sendEventMessage(principal.userId(), eventId, request);
    }

    @GetMapping
    public List<EventMessageResponse> getMessages(@PathVariable UUID eventId) {
        return messagingService.getEventMessages(eventId);
    }

    @DeleteMapping("/{messageId}")
    public void delete(@AuthenticationPrincipal AuthPrincipal principal, @PathVariable UUID eventId, @PathVariable UUID messageId) {
        messagingService.deleteEventMessage(principal.userId(), principal.role(), eventId, messageId);
    }
}

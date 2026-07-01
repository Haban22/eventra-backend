package com.eventra.backend.module.messaging.controller;

import com.eventra.backend.module.auth.security.AuthPrincipal;
import com.eventra.backend.module.messaging.dto.DMThreadResponse;
import com.eventra.backend.module.messaging.dto.DirectMessageResponse;
import com.eventra.backend.module.messaging.dto.SendDirectMessageRequest;
import com.eventra.backend.module.messaging.service.MessagingService;
import jakarta.validation.Valid;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/messages")
public class DirectMessageController {
    private final MessagingService messagingService;

    public DirectMessageController(MessagingService messagingService) {
        this.messagingService = messagingService;
    }

    @PostMapping("/direct")
    public DirectMessageResponse send(@AuthenticationPrincipal AuthPrincipal principal, @Valid @RequestBody SendDirectMessageRequest request) {
        return messagingService.sendDirectMessage(principal.userId(), request);
    }

    @GetMapping("/direct/{userId}")
    public List<DirectMessageResponse> getConversation(@AuthenticationPrincipal AuthPrincipal principal, @PathVariable UUID userId) {
        return messagingService.getConversation(principal.userId(), userId);
    }

    @GetMapping("/threads")
    public List<DMThreadResponse> getThreads(@AuthenticationPrincipal AuthPrincipal principal) {
        return messagingService.getMyThreads(principal.userId());
    }

    @PatchMapping("/direct/{userId}/read")
    public void markRead(@AuthenticationPrincipal AuthPrincipal principal, @PathVariable UUID userId) {
        messagingService.markConversationRead(principal.userId(), userId);
    }
}

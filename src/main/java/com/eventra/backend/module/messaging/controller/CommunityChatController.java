package com.eventra.backend.module.messaging.controller;

import com.eventra.backend.module.auth.security.AuthPrincipal;
import com.eventra.backend.module.messaging.dto.CommunityMessageResponse;
import com.eventra.backend.module.messaging.dto.SendMessageContentRequest;
import com.eventra.backend.module.messaging.service.MessagingService;
import jakarta.validation.Valid;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/communities/{communityId}/messages")
public class CommunityChatController {
    private final MessagingService messagingService;

    public CommunityChatController(MessagingService messagingService) {
        this.messagingService = messagingService;
    }

    @PostMapping
    public CommunityMessageResponse send(@AuthenticationPrincipal AuthPrincipal principal, @PathVariable Long communityId, @Valid @RequestBody SendMessageContentRequest request) {
        return messagingService.sendCommunityMessage(principal.userId(), communityId, request);
    }

    @GetMapping
    public List<CommunityMessageResponse> getMessages(@PathVariable Long communityId) {
        return messagingService.getCommunityMessages(communityId);
    }
}

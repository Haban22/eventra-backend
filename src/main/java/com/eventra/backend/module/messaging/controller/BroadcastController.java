package com.eventra.backend.module.messaging.controller;

import com.eventra.backend.module.auth.security.AuthPrincipal;
import com.eventra.backend.module.messaging.dto.BroadcastMessageResponse;
import com.eventra.backend.module.messaging.dto.SendBroadcastRequest;
import com.eventra.backend.module.messaging.service.MessagingService;
import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/broadcasts")
public class BroadcastController {
    private final MessagingService messagingService;

    public BroadcastController(MessagingService messagingService) {
        this.messagingService = messagingService;
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('ORGANIZER', 'ADMIN')")
    public BroadcastMessageResponse send(@AuthenticationPrincipal AuthPrincipal principal, @Valid @RequestBody SendBroadcastRequest request) {
        return messagingService.sendBroadcast(principal.userId(), principal.role(), request);
    }

    @GetMapping
    public List<BroadcastMessageResponse> getMyBroadcasts(@AuthenticationPrincipal AuthPrincipal principal) {
        return messagingService.getMyBroadcasts(principal.userId(), principal.role());
    }
}

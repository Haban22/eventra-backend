package com.eventra.backend.module.notification.controller;

import com.eventra.backend.module.auth.security.AuthPrincipal;
import com.eventra.backend.module.notification.dto.NotificationResponse;
import com.eventra.backend.module.notification.service.NotificationService;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/notifications")
public class NotificationController {
    private final NotificationService service;

    public NotificationController(NotificationService service) {
        this.service = service;
    }

    @GetMapping("/my")
    public List<NotificationResponse> getMyNotifications(@AuthenticationPrincipal AuthPrincipal principal) {
        return service.getMyNotifications(principal.userId());
    }

    @PatchMapping("/{id}/read")
    public void markRead(@AuthenticationPrincipal AuthPrincipal principal, @PathVariable UUID id) {
        service.markRead(principal.userId(), id);
    }

    @PatchMapping("/read-all")
    public void markAllRead(@AuthenticationPrincipal AuthPrincipal principal) {
        service.markAllRead(principal.userId());
    }

    @DeleteMapping("/{id}")
    public void delete(@AuthenticationPrincipal AuthPrincipal principal, @PathVariable UUID id) {
        service.delete(principal.userId(), id);
    }
}

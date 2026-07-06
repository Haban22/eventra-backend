package com.eventra.backend.module.config.controller;

import com.eventra.backend.module.auth.security.AuthPrincipal;
import com.eventra.backend.module.config.dto.SystemConfigResponse;
import com.eventra.backend.module.config.dto.UpdateSystemConfigRequest;
import com.eventra.backend.module.config.service.SystemConfigService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/admin/config")
public class SystemConfigController {
    private final SystemConfigService service;

    public SystemConfigController(SystemConfigService service) {
        this.service = service;
    }

    // Readable by any authenticated user — other roles' clients (e.g. an attendee's
    // booking-cancellation-window countdown) need this too, not just admins.
    @GetMapping
    public SystemConfigResponse get() {
        return service.getConfigResponse();
    }

    @PatchMapping
    @PreAuthorize("hasRole('ADMIN')")
    public SystemConfigResponse update(
            @AuthenticationPrincipal AuthPrincipal principal,
            @Valid @RequestBody UpdateSystemConfigRequest request,
            HttpServletRequest httpRequest) {
        return service.update(principal.userId(), request, httpRequest.getRemoteAddr());
    }
}

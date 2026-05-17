package com.eventra.backend.module.auth.controller;

import com.eventra.backend.module.auth.dto.request.PasswordChangeRequest;
import com.eventra.backend.module.auth.dto.request.UpdateMeRequest;
import com.eventra.backend.module.auth.dto.response.MessageResponse;
import com.eventra.backend.module.auth.dto.response.UserResponse;
import com.eventra.backend.module.auth.security.AuthPrincipal;
import com.eventra.backend.module.auth.service.UserService;
import jakarta.validation.Valid;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController("authUserController")
@RequestMapping("/api/v1/auth/users")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping("/me")
    public UserResponse me(@AuthenticationPrincipal AuthPrincipal principal) {
        return userService.me(principal.userId());
    }

    @PatchMapping("/me")
    public UserResponse updateMe(@AuthenticationPrincipal AuthPrincipal principal, @Valid @RequestBody UpdateMeRequest request) {
        return userService.updateMe(principal.userId(), request);
    }

    @PatchMapping("/me/password")
    public MessageResponse changePassword(@AuthenticationPrincipal AuthPrincipal principal, @Valid @RequestBody PasswordChangeRequest request) {
        userService.changePassword(principal.userId(), principal.jti(), principal.expiresAtEpochSeconds(), request);
        return new MessageResponse("Password updated");
    }
}

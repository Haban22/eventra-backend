package com.eventra.backend.module.auth.controller;

import com.eventra.backend.module.auth.dto.request.OrganizerUpgradeRequest;
import com.eventra.backend.module.auth.dto.request.OtpVerifyRequest;
import com.eventra.backend.module.auth.dto.request.PasswordChangeRequest;
import com.eventra.backend.module.auth.dto.request.UpdateMeRequest;
import com.eventra.backend.module.auth.dto.response.MessageResponse;
import com.eventra.backend.module.auth.dto.response.UserResponse;
import com.eventra.backend.module.auth.security.AuthPrincipal;
import com.eventra.backend.module.auth.service.OtpService;
import com.eventra.backend.module.auth.service.UserService;
import jakarta.validation.Valid;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController("authUserController")
@RequestMapping("/api/v1/auth/users")
public class UserController {

    private final UserService userService;
    private final OtpService otpService;

    public UserController(UserService userService, OtpService otpService) {
        this.userService = userService;
        this.otpService  = otpService;
    }

    @GetMapping("/me")
    public UserResponse me(@AuthenticationPrincipal AuthPrincipal principal) {
        return userService.me(principal.userId());
    }

    @PatchMapping("/me")
    public UserResponse updateMe(@AuthenticationPrincipal AuthPrincipal principal, @Valid @RequestBody UpdateMeRequest request) {
        return userService.updateMe(principal.userId(), request);
    }

    @PostMapping("/me/request-organizer")
    public MessageResponse requestOrganizerUpgrade(
            @AuthenticationPrincipal AuthPrincipal principal,
            @RequestBody(required = false) OrganizerUpgradeRequest request) {
        userService.requestOrganizerUpgrade(principal.userId(),
                request != null ? request.reason() : null);
        return new MessageResponse("Organizer upgrade request submitted");
    }

    @PatchMapping("/me/password")
    public MessageResponse changePassword(@AuthenticationPrincipal AuthPrincipal principal, @Valid @RequestBody PasswordChangeRequest request) {
        userService.changePassword(principal.userId(), principal.jti(), principal.expiresAtEpochSeconds(), request);
        return new MessageResponse("Password updated");
    }

    // ── Organizer onboarding OTP ──────────────────────────────────────────────

    @PostMapping("/me/onboarding/otp/request")
    public MessageResponse requestOrganizerOtp(@AuthenticationPrincipal AuthPrincipal principal) {
        otpService.requestOrganizerOtp(principal.userId());
        return new MessageResponse("OTP sent to your registered email");
    }

    @PostMapping("/me/onboarding/otp/verify")
    public MessageResponse verifyOrganizerOtp(@AuthenticationPrincipal AuthPrincipal principal,
                                               @Valid @RequestBody OtpVerifyRequest request) {
        otpService.verifyOrganizerOtp(principal.userId(), request.otp());
        return new MessageResponse("Email verified");
    }
}

package com.eventra.backend.module.auth.controller;

import com.eventra.backend.module.auth.dto.request.OrganizerUpgradeRequest;
import com.eventra.backend.module.auth.dto.request.PasswordChangeRequest;
import com.eventra.backend.module.auth.dto.request.UpdateMeRequest;
import com.eventra.backend.module.auth.dto.request.OnboardingOtpVerifyRequest;
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

    @PostMapping("/me/request-organizer")
    public MessageResponse requestOrganizerUpgrade(
            @AuthenticationPrincipal AuthPrincipal principal,
            @RequestBody(required = false) OrganizerUpgradeRequest request) {
        userService.requestOrganizerUpgrade(principal.userId(),
                request != null ? request.reason() : null);
        return new MessageResponse("Organizer upgrade request submitted");
    }

    @PostMapping("/me/onboarding/otp/request")
    public MessageResponse requestOnboardingOtp(@AuthenticationPrincipal AuthPrincipal principal) {
        userService.requestOnboardingOtp(principal.userId());
        return new MessageResponse("OTP code sent to your registered email");
    }

    @PostMapping("/me/onboarding/otp/verify")
    public MessageResponse verifyOnboardingOtp(
            @AuthenticationPrincipal AuthPrincipal principal,
            @Valid @RequestBody OnboardingOtpVerifyRequest request) {
        userService.verifyOnboardingOtp(principal.userId(), request.code());
        return new MessageResponse("Onboarding completed successfully");
    }

    @PatchMapping("/me/password")
    public MessageResponse changePassword(@AuthenticationPrincipal AuthPrincipal principal, @Valid @RequestBody PasswordChangeRequest request) {
        userService.changePassword(principal.userId(), principal.jti(), principal.expiresAtEpochSeconds(), request);
        return new MessageResponse("Password updated");
    }
}

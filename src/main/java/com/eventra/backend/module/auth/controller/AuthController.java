package com.eventra.backend.module.auth.controller;

import com.eventra.backend.module.auth.dto.request.*;
import com.eventra.backend.module.auth.dto.response.AuthResponse;
import com.eventra.backend.module.auth.dto.response.MessageResponse;
import com.eventra.backend.module.auth.security.AuthPrincipal;
import com.eventra.backend.module.auth.service.AuthService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
public class AuthController {
    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/register/attendee")
    @ResponseStatus(HttpStatus.CREATED)
    public MessageResponse registerAttendee(@Valid @RequestBody AttendeeRegistrationRequest request) {
        authService.registerAttendee(request);
        return new MessageResponse("Verification email sent");
    }

    @PostMapping("/register/organizer")
    @ResponseStatus(HttpStatus.CREATED)
    public MessageResponse registerOrganizer(@Valid @RequestBody OrganizerRegistrationRequest request) {
        authService.registerOrganizer(request);
        return new MessageResponse("Verification email sent");
    }

    @GetMapping("/verify-email")
    public MessageResponse verifyEmail(@RequestParam String token) {
        authService.verifyEmail(token);
        return new MessageResponse("Email verified");
    }

    @PostMapping("/resend-verification")
    public MessageResponse resendVerification(@Valid @RequestBody EmailRequest request) {
        authService.resendVerification(request);
        return new MessageResponse("If this account exists, a new verification email has been sent");
    }

    @PostMapping("/login")
    public AuthResponse login(@Valid @RequestBody LoginRequest request, HttpServletRequest servletRequest) {
        return authService.login(request, clientIp(servletRequest));
    }

    @PostMapping("/admin/otp/request")
    public MessageResponse requestAdminOtp(@Valid @RequestBody AdminOtpRequest request) {
        authService.requestAdminOtp(request.preAuthToken());
        return new MessageResponse("OTP code sent to your registered email");
    }

    @PostMapping("/admin/otp/verify")
    public AuthResponse verifyAdminOtp(@Valid @RequestBody AdminOtpVerifyRequest request) {
        return authService.verifyAdminOtp(request.preAuthToken(), request.code());
    }

    @PostMapping("/refresh")
    public AuthResponse refresh(@Valid @RequestBody RefreshRequest request) {
        return authService.refresh(request);
    }

    @PostMapping("/logout")
    public MessageResponse logout(@AuthenticationPrincipal AuthPrincipal principal, @RequestBody(required = false) LogoutRequest request) {
        if (principal == null) {
            return new MessageResponse("Logged out");
        }
        authService.logout(principal.jti(), principal.expiresAtEpochSeconds(), request);
        return new MessageResponse("Logged out");
    }

    @PostMapping("/forgot-password")
    public MessageResponse forgotPassword(@Valid @RequestBody EmailRequest request) {
        authService.forgotPassword(request);
        return new MessageResponse("If this email exists, a reset link has been sent");
    }

    @PostMapping("/reset-password")
    public MessageResponse resetPassword(@Valid @RequestBody ResetPasswordRequest request) {
        authService.resetPassword(request);
        return new MessageResponse("Password reset successful");
    }

    @PostMapping("/google")
    public AuthResponse google(@Valid @RequestBody GoogleLoginRequest request) {
        return authService.googleLogin(request);
    }

    private String clientIp(HttpServletRequest request) {
        return request.getRemoteAddr();
    }
}

package com.eventra.backend.module.auth.service;

import com.eventra.backend.module.auth.dto.request.*;
import com.eventra.backend.module.auth.dto.response.AuthResponse;
import com.eventra.backend.module.auth.entity.*;
import com.eventra.backend.module.auth.exception.ApiException;
import com.eventra.backend.module.auth.repository.*;
import com.eventra.backend.module.auth.security.JwtUtil;
import com.eventra.backend.module.auth.util.SecureTokenGenerator;
import com.eventra.backend.module.auth.util.TokenHashUtil;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdTokenVerifier;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionTemplate;

import java.time.Instant;
import java.util.Map;

@Service
public class AuthService {
    private static final String DUMMY_HASH = "$2a$12$ZFN0ZlMuO4rSG2P7TEh8QOsfeHDDsB5R7P0HcLnRrj6SlT82UZG0S";

    private final UserRepository userRepository;
    private final OrganizerProfileRepository organizerProfileRepository;
    private final EmailVerificationTokenRepository emailTokenRepository;
    private final PasswordResetTokenRepository passwordResetTokenRepository;
    private final RefreshTokenRepository refreshTokenRepository;
    private final AuthProviderRepository authProviderRepository;
    private final PasswordEncoder passwordEncoder;
    private final EmailService emailService;
    private final RateLimitService rateLimitService;
    private final TokenService tokenService;
    private final JwtUtil jwtUtil;
    private final GoogleIdTokenVerifier googleIdTokenVerifier;
    private final TransactionTemplate transactionTemplate;
    private final com.eventra.backend.module.auth.config.AppProperties appProperties;

    public AuthService(UserRepository userRepository, OrganizerProfileRepository organizerProfileRepository,
                       EmailVerificationTokenRepository emailTokenRepository, PasswordResetTokenRepository passwordResetTokenRepository,
                       RefreshTokenRepository refreshTokenRepository, AuthProviderRepository authProviderRepository,
                       PasswordEncoder passwordEncoder, EmailService emailService, RateLimitService rateLimitService,
                       TokenService tokenService, JwtUtil jwtUtil, GoogleIdTokenVerifier googleIdTokenVerifier,
                       TransactionTemplate transactionTemplate, com.eventra.backend.module.auth.config.AppProperties appProperties) {
        this.userRepository = userRepository;
        this.organizerProfileRepository = organizerProfileRepository;
        this.emailTokenRepository = emailTokenRepository;
        this.passwordResetTokenRepository = passwordResetTokenRepository;
        this.refreshTokenRepository = refreshTokenRepository;
        this.authProviderRepository = authProviderRepository;
        this.passwordEncoder = passwordEncoder;
        this.emailService = emailService;
        this.rateLimitService = rateLimitService;
        this.tokenService = tokenService;
        this.jwtUtil = jwtUtil;
        this.googleIdTokenVerifier = googleIdTokenVerifier;
        this.transactionTemplate = transactionTemplate;
        this.appProperties = appProperties;
    }

    @Transactional
    public void registerAttendee(AttendeeRegistrationRequest request) {
        User user = buildUser(request.fullName(), request.email(),
                request.password(), request.phone(), UserRole.ATTENDEE);
        if (request.city() != null) user.setCity(request.city());
        if (request.interests() != null) user.setInterests(request.interests());
        userRepository.save(user);
        createVerificationTokenAndSend(user);
    }

    @Transactional
    public void registerOrganizer(OrganizerRegistrationRequest request) {
        User user = buildUser(request.fullName(), request.email(),
                request.password(), request.phone(), UserRole.ORGANIZER);
        user.setProfilePictureUrl(request.profilePictureUrl());
        if (request.city() != null) user.setCity(request.city());
        userRepository.save(user);
        OrganizerProfile profile = new OrganizerProfile();
        profile.setUser(user);
        profile.setOrganizationName(
                request.organizationName() != null ? request.organizationName() : "");
        profile.setOrganizationDescription(
                request.organizationDescription() != null ? request.organizationDescription() : "");
        profile.setWebsiteUrl(blankToNull(request.websiteUrl()));
        profile.setSocialLink(blankToNull(request.socialLink()));
        profile.setExperience(request.experience());
        if (request.eventTypes() != null) profile.setEventTypes(request.eventTypes());
        organizerProfileRepository.save(profile);
        createVerificationTokenAndSend(user);
    }

    private User buildUser(String fullName, String email, String password, String phone, UserRole role) {
        String normalizedEmail = email.toLowerCase();
        if (userRepository.existsByEmail(normalizedEmail)) {
            throw new ApiException(HttpStatus.CONFLICT, "EMAIL_TAKEN", "Email already registered");
        }
        User user = new User();
        user.setFullName(fullName.trim());
        user.setEmail(normalizedEmail);
        user.setPasswordHash(passwordEncoder.encode(password));
        user.setPhone(phone);
        user.setRole(role);
        user.setStatus(UserStatus.PENDING_EMAIL_VERIFICATION);
        return user;
    }

    @Transactional
    public void verifyEmail(String rawToken) {
        var token = emailTokenRepository.findByTokenHash(TokenHashUtil.sha256(rawToken))
                .orElseThrow(() -> invalidToken());
        if (token.isUsed() || !token.getExpiresAt().isAfter(Instant.now())) {
            throw invalidToken();
        }
        User user = token.getUser();
        token.setUsed(true);
        user.setEmailVerified(true);
        user.setStatus(user.getRole() == UserRole.ORGANIZER ? UserStatus.PENDING_ADMIN_APPROVAL : UserStatus.ACTIVE);
    }

    @Transactional
    public void resendVerification(EmailRequest request) {
        String email = request.email().toLowerCase();
        if (!rateLimitService.allow("ratelimit:resend:" + email, 1, 120)) {
            throw new ApiException(HttpStatus.TOO_MANY_REQUESTS, "RATE_LIMITED", "Too many requests");
        }
        userRepository.findByEmail(email)
                .filter(user -> user.getStatus() == UserStatus.PENDING_EMAIL_VERIFICATION)
                .ifPresent(user -> {
                    emailTokenRepository.markUnusedAsUsed(user.getId());
                    createVerificationTokenAndSend(user);
                });
    }

    @Transactional(noRollbackFor = ApiException.class)
    public AuthResponse login(LoginRequest request, String ipAddress) {
        if (!rateLimitService.allow("ratelimit:login:ip:" + ipAddress, 10, 3600)) {
            throw new ApiException(HttpStatus.TOO_MANY_REQUESTS, "RATE_LIMITED", "Too many requests");
        }
        User user = userRepository.findByEmail(request.email().toLowerCase()).orElse(null);
        if (user == null) {
            passwordEncoder.matches(request.password(), DUMMY_HASH);
            throw invalidCredentials();
        }
        if (user.getStatus() != UserStatus.ACTIVE) {
            throw new ApiException(HttpStatus.FORBIDDEN, "LOGIN_BLOCKED", "Login blocked for account status", Map.of("status", user.getStatus().name()));
        }
        if (user.getLockedUntil() != null && user.getLockedUntil().isAfter(Instant.now())) {
            throw new ApiException(HttpStatus.LOCKED, "ACCOUNT_LOCKED", "Account is temporarily locked", Map.of("locked_until", user.getLockedUntil()));
        }
        if (user.getPasswordHash() == null || !passwordEncoder.matches(request.password(), user.getPasswordHash())) {
            short attempts = (short) (user.getFailedLoginAttempts() + 1);
            if (attempts >= 5) {
                user.setFailedLoginAttempts((short) 0);
                user.setLockedUntil(Instant.now().plusSeconds(900));
            } else {
                user.setFailedLoginAttempts(attempts);
            }
            throw invalidCredentials();
        }
        user.setFailedLoginAttempts((short) 0);
        user.setLockedUntil(null);
        return tokenService.issue(user, null);
    }

    @Transactional
    public AuthResponse refresh(RefreshRequest request) {
        var token = refreshTokenRepository.findByTokenHashForUpdate(TokenHashUtil.sha256(request.refreshToken()))
                .orElseThrow(() -> invalidRefresh());
        if (token.isRevoked()) {
            tokenService.revokeAllForUser(token.getUser().getId());
            throw invalidRefresh();
        }
        if (!token.getExpiresAt().isAfter(Instant.now())) {
            throw invalidRefresh();
        }
        User user = token.getUser();
        if (user.getStatus() != UserStatus.ACTIVE) {
            token.setRevoked(true);
            throw invalidRefresh();
        }
        if (jwtUtil.isBlacklisted(token.getJti())) {
            throw invalidRefresh();
        }
        token.setRevoked(true);
        Instant accessTokenExp = token.getAccessTokenExp() == null ? Instant.now().plusSeconds(1800) : token.getAccessTokenExp();
        tokenService.blacklist(token.getJti(), accessTokenExp.getEpochSecond() - Instant.now().getEpochSecond());
        return tokenService.issue(user, null);
    }

    @Transactional
    public void logout(String accessJti, long accessExpEpoch, LogoutRequest request) {
        tokenService.blacklist(accessJti, accessExpEpoch - Instant.now().getEpochSecond());
        if (request != null && request.refreshToken() != null) {
            tokenService.revokeRefreshToken(request.refreshToken());
        }
    }

    @Transactional
    public void forgotPassword(EmailRequest request) {
        String email = request.email().toLowerCase();
        if (!rateLimitService.allow("ratelimit:pwreset:" + email, 3, 3600)) {
            throw new ApiException(HttpStatus.TOO_MANY_REQUESTS, "RATE_LIMITED", "Too many requests");
        }
        userRepository.findByEmail(email)
                .filter(user -> user.getStatus() == UserStatus.ACTIVE || user.getStatus() == UserStatus.PENDING_EMAIL_VERIFICATION)
                .ifPresent(user -> {
                    passwordResetTokenRepository.markUnusedAsUsed(user.getId());
                    String raw = SecureTokenGenerator.generate();
                    PasswordResetToken token = new PasswordResetToken();
                    token.setUser(user);
                    token.setTokenHash(TokenHashUtil.sha256(raw));
                    token.setExpiresAt(Instant.now().plusSeconds(1200));
                    try {
                        emailService.sendPasswordResetEmail(user.getEmail(), raw);
                    } catch (Exception e) {
                        System.err.println("Failed to send password reset email: " + e.getMessage());
                        System.err.println("Password reset raw token (for dev): " + raw);
                    }
                });
    }

    @Transactional
    public void resetPassword(ResetPasswordRequest request) {
        if (!request.newPassword().equals(request.confirmPassword())) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "PASSWORDS_DO_NOT_MATCH", "Passwords do not match");
        }
        var token = passwordResetTokenRepository.findByTokenHash(TokenHashUtil.sha256(request.token()))
                .orElseThrow(() -> invalidToken());
        if (token.isUsed() || !token.getExpiresAt().isAfter(Instant.now())) {
            throw invalidToken();
        }
        User user = token.getUser();
        user.setPasswordHash(passwordEncoder.encode(request.newPassword()));
        token.setUsed(true);
        tokenService.revokeAllForUser(user.getId());
    }

    public AuthResponse googleLogin(GoogleLoginRequest request) {
        GoogleIdToken.Payload payload = verifyGoogleToken(request.idToken());
        if (!Boolean.TRUE.equals(payload.getEmailVerified())) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "INVALID_GOOGLE_TOKEN", "Google account email is not verified");
        }
        OAuthLoginResult result;
        try {
            result = transactionTemplate.execute(status -> resolveGoogleUser(payload));
        } catch (DataIntegrityViolationException ex) {
            result = transactionTemplate.execute(status -> resolveGoogleUser(payload));
        }
        User user = result.user();
        if (user.getStatus() != UserStatus.ACTIVE) {
            throw new ApiException(HttpStatus.FORBIDDEN, "LOGIN_BLOCKED", "Login blocked for account status", Map.of("status", user.getStatus().name()));
        }
        return tokenService.issue(user, result.created());
    }

    private OAuthLoginResult resolveGoogleUser(GoogleIdToken.Payload payload) {
        String googleSub = payload.getSubject();
        String email = payload.getEmail().toLowerCase();
        var provider = authProviderRepository.findByProviderAndProviderUserId("GOOGLE", googleSub);
        if (provider.isPresent()) {
            return new OAuthLoginResult(provider.get().getUser(), false);
        }
        var existingUser = userRepository.findByEmail(email);
        User linked = existingUser.orElseGet(() -> {
            User newUser = new User();
            newUser.setFullName((String) payload.get("name"));
            newUser.setEmail(email);
            newUser.setProfilePictureUrl((String) payload.get("picture"));
            newUser.setRole(UserRole.ATTENDEE);
            newUser.setStatus(UserStatus.ACTIVE);
            newUser.setEmailVerified(true);
            return userRepository.saveAndFlush(newUser);
        });
        AuthProvider authProvider = new AuthProvider();
        authProvider.setUser(linked);
        authProvider.setProvider("GOOGLE");
        authProvider.setProviderUserId(googleSub);
        authProvider.setProviderEmail(email);
        authProviderRepository.saveAndFlush(authProvider);
        return new OAuthLoginResult(linked, existingUser.isEmpty());
    }

    private GoogleIdToken.Payload verifyGoogleToken(String idToken) {
        try {
            if (appProperties.googleClientId() == null || appProperties.googleClientId().isBlank()) {
                throw new ApiException(HttpStatus.BAD_REQUEST, "INVALID_GOOGLE_TOKEN", "Google client ID is not configured");
            }
            GoogleIdToken token = googleIdTokenVerifier.verify(idToken);
            if (token == null) {
                throw new ApiException(HttpStatus.BAD_REQUEST, "INVALID_GOOGLE_TOKEN", "Invalid Google token");
            }
            return token.getPayload();
        } catch (ApiException e) {
            throw e;
        } catch (Exception e) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "INVALID_GOOGLE_TOKEN", "Invalid Google token");
        }
    }

    private void createVerificationTokenAndSend(User user) {
        String raw = SecureTokenGenerator.generate();
        EmailVerificationToken token = new EmailVerificationToken();
        token.setUser(user);
        token.setTokenHash(TokenHashUtil.sha256(raw));
        token.setExpiresAt(Instant.now().plusSeconds(86_400));
        emailTokenRepository.save(token);
        
        try {
            emailService.sendVerificationEmail(user.getEmail(), raw);
        } catch (Exception e) {
            System.err.println("Failed to send verification email: " + e.getMessage());
            System.err.println("Verification raw token (for dev): " + raw);
        }
    }

    private ApiException invalidCredentials() {
        return new ApiException(HttpStatus.UNAUTHORIZED, "INVALID_CREDENTIALS", "Invalid email or password");
    }

    private ApiException invalidRefresh() {
        return new ApiException(HttpStatus.UNAUTHORIZED, "INVALID_REFRESH_TOKEN", "Invalid refresh token");
    }

    private ApiException invalidToken() {
        return new ApiException(HttpStatus.BAD_REQUEST, "INVALID_TOKEN", "Invalid token");
    }

    private String blankToNull(String value) {
        return value == null || value.isBlank() ? null : value;
    }

    private record OAuthLoginResult(User user, boolean created) {
    }
}

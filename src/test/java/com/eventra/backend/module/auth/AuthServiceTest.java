package com.eventra.backend.module.auth;

import com.eventra.backend.module.auth.config.AppProperties;
import com.eventra.backend.module.auth.dto.request.AttendeeRegistrationRequest;
import com.eventra.backend.module.auth.dto.request.OrganizerRegistrationRequest;
import com.eventra.backend.module.auth.entity.User;
import com.eventra.backend.module.auth.entity.UserRole;
import com.eventra.backend.module.auth.entity.UserStatus;
import com.eventra.backend.module.auth.entity.EmailVerificationToken;
import com.eventra.backend.module.auth.entity.RefreshToken;
import com.eventra.backend.module.auth.dto.response.AuthResponse;
import com.eventra.backend.module.auth.exception.ApiException;
import com.eventra.backend.module.auth.repository.*;
import com.eventra.backend.module.auth.security.JwtUtil;
import com.eventra.backend.module.auth.service.*;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdTokenVerifier;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.transaction.support.TransactionTemplate;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private UserRepository userRepository;
    @Mock
    private OrganizerProfileRepository organizerProfileRepository;
    @Mock
    private EmailVerificationTokenRepository emailTokenRepository;
    @Mock
    private PasswordResetTokenRepository passwordResetTokenRepository;
    @Mock
    private RefreshTokenRepository refreshTokenRepository;
    @Mock
    private AuthProviderRepository authProviderRepository;
    @Mock
    private PasswordEncoder passwordEncoder;
    @Mock
    private EmailService emailService;
    @Mock
    private RateLimitService rateLimitService;
    @Mock
    private TokenService tokenService;
    @Mock
    private JwtUtil jwtUtil;
    @Mock
    private GoogleIdTokenVerifier googleIdTokenVerifier;
    @Mock
    private TransactionTemplate transactionTemplate;
    @Mock
    private AppProperties appProperties;
    @Mock
    private OtpService otpService;

    private AuthService authService;

    @BeforeEach
    void setUp() {
        authService = new AuthService(
                userRepository, organizerProfileRepository, emailTokenRepository,
                passwordResetTokenRepository, refreshTokenRepository, authProviderRepository,
                passwordEncoder, emailService, rateLimitService, tokenService,
                jwtUtil, googleIdTokenVerifier, transactionTemplate, appProperties,
                otpService
        );
    }

    @Test
    void registerAttendee_Success() {
        // Given
        AttendeeRegistrationRequest request = new AttendeeRegistrationRequest(
                "John Doe", "john@example.com", "Password123", "+1234567890",
                "New York", java.util.List.of("Sports", "Tech", "Music")
        );
        when(userRepository.existsByEmail(any())).thenReturn(false);
        when(passwordEncoder.encode(any())).thenReturn("hashedPassword");

        // When
        authService.registerAttendee(request);

        // Then
        ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
        verify(userRepository).save(userCaptor.capture());
        User savedUser = userCaptor.getValue();

        assertEquals("John Doe", savedUser.getFullName());
        assertEquals("john@example.com", savedUser.getEmail());
        assertEquals("hashedPassword", savedUser.getPasswordHash());
        assertEquals(UserRole.ATTENDEE, savedUser.getRole());
        assertEquals(UserStatus.PENDING_EMAIL_VERIFICATION, savedUser.getStatus());

        verify(emailTokenRepository).save(any());
        verify(emailService).sendVerificationEmail(eq("john@example.com"), any());
    }

    @Test
    void registerAttendee_EmailAlreadyExists_ThrowsConflict() {
        // Given
        AttendeeRegistrationRequest request = new AttendeeRegistrationRequest(
                "John Doe", "john@example.com", "Password123", "+1234567890",
                "New York", java.util.List.of("Sports", "Tech", "Music")
        );
        when(userRepository.existsByEmail("john@example.com")).thenReturn(true);

        // When & Then
        ApiException exception = assertThrows(ApiException.class, () -> authService.registerAttendee(request));
        assertEquals(HttpStatus.CONFLICT, exception.getStatus());
        assertEquals("EMAIL_TAKEN", exception.getError());
    }

    @Test
    void registerOrganizer_Success() {
        // Given
        OrganizerRegistrationRequest request = new OrganizerRegistrationRequest(
                "Jane Org", "jane@org.com", "Password123", "+1234567890",
                "San Francisco", "Org Name", "Org Desc", "https://org.com", 
                "https://social.com", "https://profile.com", "5 years",
                java.util.List.of("Tech", "Conferences")
        );
        when(userRepository.existsByEmail(any())).thenReturn(false);
        when(passwordEncoder.encode(any())).thenReturn("hashedPassword");

        // When
        authService.registerOrganizer(request);

        // Then
        verify(userRepository).save(any());
        verify(organizerProfileRepository).save(any());
        verify(emailTokenRepository).save(any());
        verify(emailService).sendVerificationEmail(eq("jane@org.com"), any());
    }

    @Test
    void verifyEmail_Success_Attendee() {
        // Given
        String rawToken = "raw_token_here";
        String hashedToken = com.eventra.backend.module.auth.util.TokenHashUtil.sha256(rawToken);
        User user = new User();
        user.setRole(UserRole.ATTENDEE);
        user.setStatus(UserStatus.PENDING_EMAIL_VERIFICATION);
        user.setEmailVerified(false);

        EmailVerificationToken token = new EmailVerificationToken();
        token.setUser(user);
        token.setTokenHash(hashedToken);
        token.setExpiresAt(java.time.Instant.now().plusSeconds(3600));
        token.setUsed(false);

        when(emailTokenRepository.findByTokenHash(hashedToken)).thenReturn(java.util.Optional.of(token));

        // When
        authService.verifyEmail(rawToken);

        // Then
        assertTrue(token.isUsed());
        assertTrue(user.isEmailVerified());
        assertEquals(UserStatus.ACTIVE, user.getStatus());
    }

    @Test
    void verifyEmail_Success_Organizer() {
        // Given
        String rawToken = "raw_token_here";
        String hashedToken = com.eventra.backend.module.auth.util.TokenHashUtil.sha256(rawToken);
        User user = new User();
        user.setRole(UserRole.ORGANIZER);
        user.setStatus(UserStatus.PENDING_EMAIL_VERIFICATION);
        user.setEmailVerified(false);

        EmailVerificationToken token = new EmailVerificationToken();
        token.setUser(user);
        token.setTokenHash(hashedToken);
        token.setExpiresAt(java.time.Instant.now().plusSeconds(3600));
        token.setUsed(false);

        when(emailTokenRepository.findByTokenHash(hashedToken)).thenReturn(java.util.Optional.of(token));

        // When
        authService.verifyEmail(rawToken);

        // Then
        assertTrue(token.isUsed());
        assertTrue(user.isEmailVerified());
        assertEquals(UserStatus.PENDING_ADMIN_APPROVAL, user.getStatus());
    }

    @Test
    void verifyEmail_TokenExpired_ThrowsBadRequest() {
        // Given
        String rawToken = "raw_token_here";
        String hashedToken = com.eventra.backend.module.auth.util.TokenHashUtil.sha256(rawToken);
        User user = new User();

        EmailVerificationToken token = new EmailVerificationToken();
        token.setUser(user);
        token.setTokenHash(hashedToken);
        token.setExpiresAt(java.time.Instant.now().minusSeconds(10)); // expired
        token.setUsed(false);

        when(emailTokenRepository.findByTokenHash(hashedToken)).thenReturn(java.util.Optional.of(token));

        // When & Then
        ApiException exception = assertThrows(ApiException.class, () -> authService.verifyEmail(rawToken));
        assertEquals(HttpStatus.BAD_REQUEST, exception.getStatus());
        assertEquals("INVALID_TOKEN", exception.getError());
    }

    @Test
    void verifyEmail_TokenAlreadyUsed_ThrowsBadRequest() {
        // Given
        String rawToken = "raw_token_here";
        String hashedToken = com.eventra.backend.module.auth.util.TokenHashUtil.sha256(rawToken);
        User user = new User();

        EmailVerificationToken token = new EmailVerificationToken();
        token.setUser(user);
        token.setTokenHash(hashedToken);
        token.setExpiresAt(java.time.Instant.now().plusSeconds(3600));
        token.setUsed(true); // already used

        when(emailTokenRepository.findByTokenHash(hashedToken)).thenReturn(java.util.Optional.of(token));

        // When & Then
        ApiException exception = assertThrows(ApiException.class, () -> authService.verifyEmail(rawToken));
        assertEquals(HttpStatus.BAD_REQUEST, exception.getStatus());
        assertEquals("INVALID_TOKEN", exception.getError());
    }

    @Test
    void login_Success() {
        // Given
        com.eventra.backend.module.auth.dto.request.LoginRequest request = 
                new com.eventra.backend.module.auth.dto.request.LoginRequest("john@example.com", "Password123");
        String ipAddress = "127.0.0.1";

        User user = new User();
        user.setEmail("john@example.com");
        user.setPasswordHash("hashed_pwd");
        user.setStatus(UserStatus.ACTIVE);
        user.setFailedLoginAttempts((short) 3);

        when(rateLimitService.allow(eq("ratelimit:login:ip:" + ipAddress), anyInt(), anyLong())).thenReturn(true);
        when(userRepository.findByEmail("john@example.com")).thenReturn(java.util.Optional.of(user));
        when(passwordEncoder.matches("Password123", "hashed_pwd")).thenReturn(true);
        
        AuthResponse mockResponse = 
                new AuthResponse("access_token", "refresh_token", "Bearer", 1800L, null, null);
        when(tokenService.issue(user, null)).thenReturn(mockResponse);

        // When
        com.eventra.backend.module.auth.dto.response.AuthResponse response = authService.login(request, ipAddress);

        // Then
        assertNotNull(response);
        assertEquals("access_token", response.accessToken());
        assertEquals(0, user.getFailedLoginAttempts());
        assertNull(user.getLockedUntil());
    }

    @Test
    void login_RateLimited_ThrowsTooManyRequests() {
        // Given
        com.eventra.backend.module.auth.dto.request.LoginRequest request = 
                new com.eventra.backend.module.auth.dto.request.LoginRequest("john@example.com", "Password123");
        String ipAddress = "127.0.0.1";

        when(rateLimitService.allow(eq("ratelimit:login:ip:" + ipAddress), anyInt(), anyLong())).thenReturn(false);

        // When & Then
        ApiException exception = assertThrows(ApiException.class, () -> authService.login(request, ipAddress));
        assertEquals(HttpStatus.TOO_MANY_REQUESTS, exception.getStatus());
        assertEquals("RATE_LIMITED", exception.getError());
    }

    @Test
    void login_NotActive_ThrowsForbidden() {
        // Given
        com.eventra.backend.module.auth.dto.request.LoginRequest request = 
                new com.eventra.backend.module.auth.dto.request.LoginRequest("john@example.com", "Password123");
        String ipAddress = "127.0.0.1";

        User user = new User();
        user.setEmail("john@example.com");
        user.setStatus(UserStatus.PENDING_EMAIL_VERIFICATION);

        when(rateLimitService.allow(eq("ratelimit:login:ip:" + ipAddress), anyInt(), anyLong())).thenReturn(true);
        when(userRepository.findByEmail("john@example.com")).thenReturn(java.util.Optional.of(user));

        // When & Then
        ApiException exception = assertThrows(ApiException.class, () -> authService.login(request, ipAddress));
        assertEquals(HttpStatus.FORBIDDEN, exception.getStatus());
        assertEquals("LOGIN_BLOCKED", exception.getError());
    }

    @Test
    void login_AccountLocked_ThrowsLocked() {
        // Given
        com.eventra.backend.module.auth.dto.request.LoginRequest request = 
                new com.eventra.backend.module.auth.dto.request.LoginRequest("john@example.com", "Password123");
        String ipAddress = "127.0.0.1";

        User user = new User();
        user.setEmail("john@example.com");
        user.setStatus(UserStatus.ACTIVE);
        user.setLockedUntil(java.time.Instant.now().plusSeconds(600));

        when(rateLimitService.allow(eq("ratelimit:login:ip:" + ipAddress), anyInt(), anyLong())).thenReturn(true);
        when(userRepository.findByEmail("john@example.com")).thenReturn(java.util.Optional.of(user));

        // When & Then
        ApiException exception = assertThrows(ApiException.class, () -> authService.login(request, ipAddress));
        assertEquals(HttpStatus.LOCKED, exception.getStatus());
        assertEquals("ACCOUNT_LOCKED", exception.getError());
    }

    @Test
    void login_WrongPassword_IncrementsFailedAttempts() {
        // Given
        com.eventra.backend.module.auth.dto.request.LoginRequest request = 
                new com.eventra.backend.module.auth.dto.request.LoginRequest("john@example.com", "WrongPwd");
        String ipAddress = "127.0.0.1";

        User user = new User();
        user.setEmail("john@example.com");
        user.setPasswordHash("hashed_pwd");
        user.setStatus(UserStatus.ACTIVE);
        user.setFailedLoginAttempts((short) 2);

        when(rateLimitService.allow(eq("ratelimit:login:ip:" + ipAddress), anyInt(), anyLong())).thenReturn(true);
        when(userRepository.findByEmail("john@example.com")).thenReturn(java.util.Optional.of(user));
        when(passwordEncoder.matches("WrongPwd", "hashed_pwd")).thenReturn(false);

        // When & Then
        ApiException exception = assertThrows(ApiException.class, () -> authService.login(request, ipAddress));
        assertEquals(HttpStatus.UNAUTHORIZED, exception.getStatus());
        assertEquals("INVALID_CREDENTIALS", exception.getError());
        assertEquals(3, user.getFailedLoginAttempts());
        assertNull(user.getLockedUntil());
    }

    @Test
    void login_WrongPassword_LocksAccount_On5thAttempt() {
        // Given
        com.eventra.backend.module.auth.dto.request.LoginRequest request = 
                new com.eventra.backend.module.auth.dto.request.LoginRequest("john@example.com", "WrongPwd");
        String ipAddress = "127.0.0.1";

        User user = new User();
        user.setEmail("john@example.com");
        user.setPasswordHash("hashed_pwd");
        user.setStatus(UserStatus.ACTIVE);
        user.setFailedLoginAttempts((short) 4);

        when(rateLimitService.allow(eq("ratelimit:login:ip:" + ipAddress), anyInt(), anyLong())).thenReturn(true);
        when(userRepository.findByEmail("john@example.com")).thenReturn(java.util.Optional.of(user));
        when(passwordEncoder.matches("WrongPwd", "hashed_pwd")).thenReturn(false);

        // When & Then
        ApiException exception = assertThrows(ApiException.class, () -> authService.login(request, ipAddress));
        assertEquals(HttpStatus.UNAUTHORIZED, exception.getStatus());
        assertEquals(0, user.getFailedLoginAttempts());
        assertNotNull(user.getLockedUntil());
        assertTrue(user.getLockedUntil().isAfter(java.time.Instant.now()));
    }

    @Test
    void refresh_Success() {
        // Given
        com.eventra.backend.module.auth.dto.request.RefreshRequest request = 
                new com.eventra.backend.module.auth.dto.request.RefreshRequest("refresh_token_val");
        String hashedToken = com.eventra.backend.module.auth.util.TokenHashUtil.sha256("refresh_token_val");

        User user = new User();
        user.setStatus(UserStatus.ACTIVE);

        com.eventra.backend.module.auth.entity.RefreshToken token = new com.eventra.backend.module.auth.entity.RefreshToken();
        token.setUser(user);
        token.setTokenHash(hashedToken);
        token.setJti("some_jti");
        token.setExpiresAt(java.time.Instant.now().plusSeconds(3600));
        token.setAccessTokenExp(java.time.Instant.now().plusSeconds(1800));
        token.setRevoked(false);

        when(refreshTokenRepository.findByTokenHashForUpdate(hashedToken)).thenReturn(java.util.Optional.of(token));
        when(jwtUtil.isBlacklisted("some_jti")).thenReturn(false);

        AuthResponse mockResponse = 
                new AuthResponse("new_access", "new_refresh", "Bearer", 1800L, null, null);
        when(tokenService.issue(user, null)).thenReturn(mockResponse);

        // When
        com.eventra.backend.module.auth.dto.response.AuthResponse response = authService.refresh(request);

        // Then
        assertNotNull(response);
        assertEquals("new_access", response.accessToken());
        assertTrue(token.isRevoked());
        verify(tokenService).blacklist(eq("some_jti"), anyLong());
    }

    @Test
    void refresh_RevokedToken_RevokesAllForUser_ThrowsUnauthorized() {
        // Given
        com.eventra.backend.module.auth.dto.request.RefreshRequest request = 
                new com.eventra.backend.module.auth.dto.request.RefreshRequest("refresh_token_val");
        String hashedToken = com.eventra.backend.module.auth.util.TokenHashUtil.sha256("refresh_token_val");

        User user = new User();
        user.setId(java.util.UUID.randomUUID());

        com.eventra.backend.module.auth.entity.RefreshToken token = new com.eventra.backend.module.auth.entity.RefreshToken();
        token.setUser(user);
        token.setTokenHash(hashedToken);
        token.setRevoked(true); // already revoked

        when(refreshTokenRepository.findByTokenHashForUpdate(hashedToken)).thenReturn(java.util.Optional.of(token));

        // When & Then
        ApiException exception = assertThrows(ApiException.class, () -> authService.refresh(request));
        assertEquals(HttpStatus.UNAUTHORIZED, exception.getStatus());
        assertEquals("INVALID_REFRESH_TOKEN", exception.getError());
        verify(tokenService).revokeAllForUser(user.getId());
    }

    @Test
    void logout_Success() {
        // Given
        String jti = "some_access_jti";
        long exp = java.time.Instant.now().getEpochSecond() + 1800;
        com.eventra.backend.module.auth.dto.request.LogoutRequest request = 
                new com.eventra.backend.module.auth.dto.request.LogoutRequest("refresh_token_val");

        // When
        authService.logout(jti, exp, request);

        // Then
        verify(tokenService).blacklist(eq(jti), anyLong());
        verify(tokenService).revokeRefreshToken("refresh_token_val");
    }
}

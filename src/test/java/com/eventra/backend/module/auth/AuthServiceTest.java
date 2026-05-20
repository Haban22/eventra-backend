package com.eventra.backend.module.auth;

import com.eventra.backend.module.auth.config.AppProperties;
import com.eventra.backend.module.auth.dto.request.AttendeeRegistrationRequest;
import com.eventra.backend.module.auth.dto.request.OrganizerRegistrationRequest;
import com.eventra.backend.module.auth.entity.User;
import com.eventra.backend.module.auth.entity.UserRole;
import com.eventra.backend.module.auth.entity.UserStatus;
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

    private AuthService authService;

    @BeforeEach
    void setUp() {
        authService = new AuthService(
                userRepository, organizerProfileRepository, emailTokenRepository,
                passwordResetTokenRepository, refreshTokenRepository, authProviderRepository,
                passwordEncoder, emailService, rateLimitService, tokenService,
                jwtUtil, googleIdTokenVerifier, transactionTemplate, appProperties
        );
    }

    @Test
    void registerAttendee_Success() {
        // Given
        AttendeeRegistrationRequest request = new AttendeeRegistrationRequest(
                "John Doe", "john@example.com", "Password123", "+1234567890"
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
                "John Doe", "john@example.com", "Password123", "+1234567890"
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
                "Org Name", "Org Desc", "https://org.com", null, null
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
}

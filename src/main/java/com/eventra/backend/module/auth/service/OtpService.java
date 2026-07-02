package com.eventra.backend.module.auth.service;

import com.eventra.backend.module.auth.config.AppProperties;
import com.eventra.backend.module.auth.entity.OtpCode;
import com.eventra.backend.module.auth.entity.User;
import com.eventra.backend.module.auth.entity.UserRole;
import com.eventra.backend.module.auth.exception.ApiException;
import com.eventra.backend.module.auth.repository.OtpCodeRepository;
import com.eventra.backend.module.auth.repository.UserRepository;
import com.eventra.backend.module.auth.util.TokenHashUtil;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.time.Duration;
import java.time.Instant;
import java.util.UUID;

@Service
public class OtpService {

    private static final String PURPOSE_ADMIN_LOGIN          = "ADMIN_LOGIN";
    private static final String PURPOSE_ORGANIZER_ONBOARDING = "ORGANIZER_ONBOARDING";
    private static final int    OTP_EXPIRY_SECONDS           = 600; // 10 min
    private static final String PRE_AUTH_PREFIX              = "preauth:";

    private final OtpCodeRepository     otpCodeRepository;
    private final UserRepository        userRepository;
    private final EmailService          emailService;
    private final TokenService          tokenService;
    private final RateLimitService      rateLimitService;
    private final StringRedisTemplate   redisTemplate;
    private final AppProperties         appProperties;
    private final SecureRandom          random = new SecureRandom();

    public OtpService(OtpCodeRepository otpCodeRepository,
                      UserRepository userRepository,
                      EmailService emailService,
                      TokenService tokenService,
                      RateLimitService rateLimitService,
                      StringRedisTemplate redisTemplate,
                      AppProperties appProperties) {
        this.otpCodeRepository = otpCodeRepository;
        this.userRepository    = userRepository;
        this.emailService      = emailService;
        this.tokenService      = tokenService;
        this.rateLimitService  = rateLimitService;
        this.redisTemplate     = redisTemplate;
        this.appProperties     = appProperties;
    }

    // ── Admin 2FA ─────────────────────────────────────────────────────────────

    /** Called from AuthService.login when user is ADMIN — generates a pre-auth token and returns it */
    @Transactional
    public String createPreAuthToken(UUID userId) {
        String preAuthToken = UUID.randomUUID().toString();
        String key = PRE_AUTH_PREFIX + TokenHashUtil.sha256(preAuthToken);
        redisTemplate.opsForValue().set(key, userId.toString(), Duration.ofSeconds(OTP_EXPIRY_SECONDS));
        return preAuthToken;
    }

    /** POST /auth/admin/otp/request — send 6-digit OTP to admin's email */
    @Transactional
    public void requestAdminOtp(String preAuthToken) {
        UUID userId = resolvePreAuthToken(preAuthToken);
        User user = loadUser(userId);
        if (user.getRole() != UserRole.ADMIN) {
            throw new ApiException(HttpStatus.FORBIDDEN, "FORBIDDEN", "Not an admin account");
        }
        if (!rateLimitService.allow("ratelimit:admin-otp:" + userId, 3, 600)) {
            throw new ApiException(HttpStatus.TOO_MANY_REQUESTS, "RATE_LIMITED", "Too many OTP requests");
        }
        String code = sendOtp(user, PURPOSE_ADMIN_LOGIN);
        System.out.println("[DEV] Admin OTP for " + user.getEmail() + ": " + code);
    }

    /** POST /auth/admin/otp/verify — verify OTP and return full auth tokens */
    @Transactional
    public com.eventra.backend.module.auth.dto.response.AuthResponse verifyAdminOtp(
            String preAuthToken, String otp) {
        UUID userId = resolvePreAuthToken(preAuthToken);
        User user = loadUser(userId);

        if (appProperties.skipEmailVerification()) {
            // Dev mode: any 6-digit code or the literal "000000" works
            if (!"000000".equals(otp) && !otp.matches("\\d{6}")) {
                throw new ApiException(HttpStatus.BAD_REQUEST, "INVALID_OTP", "Invalid OTP");
            }
        } else {
            verifyOtp(userId, PURPOSE_ADMIN_LOGIN, otp);
        }

        // Invalidate pre-auth token
        redisTemplate.delete(PRE_AUTH_PREFIX + TokenHashUtil.sha256(preAuthToken));
        return tokenService.issue(user, null);
    }

    // ── Organizer onboarding OTP ───────────────────────────────────────────────

    /** POST /auth/organizer/otp/request — send OTP to organizer's email */
    @Transactional
    public void requestOrganizerOtp(UUID userId) {
        User user = loadUser(userId);
        if (!rateLimitService.allow("ratelimit:org-otp:" + userId, 3, 600)) {
            throw new ApiException(HttpStatus.TOO_MANY_REQUESTS, "RATE_LIMITED", "Too many OTP requests");
        }
        String code = sendOtp(user, PURPOSE_ORGANIZER_ONBOARDING);
        System.out.println("[DEV] Organizer OTP for " + user.getEmail() + ": " + code);
    }

    /** POST /auth/organizer/otp/verify — verify OTP and mark organizer email verified + onboarding done */
    @Transactional
    public void verifyOrganizerOtp(UUID userId, String otp) {
        User user = loadUser(userId);

        if (appProperties.skipEmailVerification()) {
            if (!"000000".equals(otp) && !otp.matches("\\d{6}")) {
                throw new ApiException(HttpStatus.BAD_REQUEST, "INVALID_OTP", "Invalid OTP");
            }
        } else {
            verifyOtp(userId, PURPOSE_ORGANIZER_ONBOARDING, otp);
        }

        user.setOnboardingCompleted(true);
    }

    // ── Internals ─────────────────────────────────────────────────────────────

    private String sendOtp(User user, String purpose) {
        otpCodeRepository.markAllUsed(user.getId(), purpose);
        String code = String.format("%06d", random.nextInt(1_000_000));
        OtpCode otpCode = new OtpCode();
        otpCode.setUser(user);
        otpCode.setPurpose(purpose);
        otpCode.setCodeHash(TokenHashUtil.sha256(code));
        otpCode.setExpiresAt(Instant.now().plusSeconds(OTP_EXPIRY_SECONDS));
        otpCodeRepository.save(otpCode);

        try {
            emailService.sendOtpEmail(user.getEmail(), code, purpose);
        } catch (Exception e) {
            System.err.println("[MAIL] Failed to send OTP email: " + e.getMessage());
        }
        return code;
    }

    private void verifyOtp(UUID userId, String purpose, String code) {
        String hash = TokenHashUtil.sha256(code);
        OtpCode otp = otpCodeRepository.findByCodeHash(hash)
                .orElseThrow(() -> new ApiException(HttpStatus.BAD_REQUEST, "INVALID_OTP", "Invalid or expired OTP"));
        if (otp.isUsed() || !otp.getExpiresAt().isAfter(Instant.now())
                || !otp.getUser().getId().equals(userId)
                || !otp.getPurpose().equals(purpose)) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "INVALID_OTP", "Invalid or expired OTP");
        }
        otp.setUsed(true);
    }

    private UUID resolvePreAuthToken(String preAuthToken) {
        String key   = PRE_AUTH_PREFIX + TokenHashUtil.sha256(preAuthToken);
        String value = redisTemplate.opsForValue().get(key);
        if (value == null) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "INVALID_PRE_AUTH_TOKEN", "Pre-auth token expired or invalid");
        }
        return UUID.fromString(value);
    }

    private User loadUser(UUID userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "NOT_FOUND", "User not found"));
    }
}

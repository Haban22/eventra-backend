package com.eventra.backend.module.auth.service;

import com.eventra.backend.module.auth.config.AppProperties;
import com.eventra.backend.module.auth.entity.OtpCode;
import com.eventra.backend.module.auth.entity.User;
import com.eventra.backend.module.auth.exception.ApiException;
import com.eventra.backend.module.auth.repository.OtpCodeRepository;
import com.eventra.backend.module.auth.repository.UserRepository;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.time.Instant;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

@Service
public class OtpService {
    private final OtpCodeRepository otpCodeRepository;
    private final UserRepository userRepository;
    private final StringRedisTemplate redisTemplate;
    private final EmailService emailService;
    private final AppProperties appProperties;
    private final SecureRandom random = new SecureRandom();

    public OtpService(OtpCodeRepository otpCodeRepository,
                      UserRepository userRepository,
                      StringRedisTemplate redisTemplate,
                      EmailService emailService,
                      AppProperties appProperties) {
        this.otpCodeRepository = otpCodeRepository;
        this.userRepository = userRepository;
        this.redisTemplate = redisTemplate;
        this.emailService = emailService;
        this.appProperties = appProperties;
    }

    @Transactional
    public void generateOtp(User user) {
        String code = String.format("%06d", random.nextInt(1000000));
        
        OtpCode otp = new OtpCode();
        otp.setUserId(user.getId());
        otp.setCode(code);
        otp.setExpiresAt(Instant.now().plusSeconds(300));
        otpCodeRepository.save(otp);

        if (appProperties.skipEmailVerification()) {
            System.out.println("[DEV] Admin OTP for " + user.getEmail() + ": " + code);
            writeOtpToProjectFile(code);
        } else {
            emailService.sendOtpEmail(user.getEmail(), code);
        }
    }

    public String createPreAuthToken(User user) {
        String token = UUID.randomUUID().toString();
        redisTemplate.opsForValue().set("pre-auth:" + token, user.getId().toString(), 300, TimeUnit.SECONDS);
        return token;
    }

    @Transactional
    public void requestAdminOtp(String preAuthToken) {
        String userIdStr = redisTemplate.opsForValue().get("pre-auth:" + preAuthToken);
        if (userIdStr == null) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "INVALID_PRE_AUTH_TOKEN", "Pre-authentication token has expired or is invalid");
        }
        UUID userId = UUID.fromString(userIdStr);
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "USER_NOT_FOUND", "User not found"));
        generateOtp(user);
    }

    @Transactional
    public User verifyAdminOtp(String preAuthToken, String code) {
        String userIdStr = redisTemplate.opsForValue().get("pre-auth:" + preAuthToken);
        if (userIdStr == null) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "INVALID_PRE_AUTH_TOKEN", "Pre-authentication token has expired or is invalid");
        }
        UUID userId = UUID.fromString(userIdStr);
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "USER_NOT_FOUND", "User not found"));

        if (appProperties.skipEmailVerification() && ("000000".equals(code) || (code != null && code.length() == 6))) {
            // Bypass successful
        } else {
            OtpCode otp = otpCodeRepository.findTopByUserIdAndUsedFalseOrderByCreatedAtDesc(userId)
                    .orElseThrow(() -> new ApiException(HttpStatus.BAD_REQUEST, "INVALID_OTP", "Invalid or expired OTP code"));

            if (otp.getExpiresAt().isBefore(Instant.now()) || !otp.getCode().equals(code)) {
                throw new ApiException(HttpStatus.BAD_REQUEST, "INVALID_OTP", "Invalid or expired OTP code");
            }
            otp.setUsed(true);
            otpCodeRepository.save(otp);
        }

        redisTemplate.delete("pre-auth:" + preAuthToken);
        return user;
    }

    @Transactional
    public void generateOnboardingOtp(User user) {
        String code = String.format("%06d", random.nextInt(1000000));

        OtpCode otp = new OtpCode();
        otp.setUserId(user.getId());
        otp.setCode(code);
        otp.setExpiresAt(Instant.now().plusSeconds(300));
        otpCodeRepository.save(otp);

        if (appProperties.skipEmailVerification()) {
            System.out.println("[DEV] Onboarding OTP for " + user.getEmail() + ": " + code);
            writeOtpToProjectFile(code);
        } else {
            emailService.sendOtpEmail(user.getEmail(), code);
        }
    }

    @Transactional
    public void verifyOnboardingOtp(User user, String code) {
        if (appProperties.skipEmailVerification() && ("000000".equals(code) || (code != null && code.length() == 6))) {
            // Bypass successful
        } else {
            OtpCode otp = otpCodeRepository.findTopByUserIdAndUsedFalseOrderByCreatedAtDesc(user.getId())
                    .orElseThrow(() -> new ApiException(HttpStatus.BAD_REQUEST, "INVALID_OTP", "Invalid or expired OTP code"));

            if (otp.getExpiresAt().isBefore(Instant.now()) || !otp.getCode().equals(code)) {
                throw new ApiException(HttpStatus.BAD_REQUEST, "INVALID_OTP", "Invalid or expired OTP code");
            }
            otp.setUsed(true);
            otpCodeRepository.save(otp);
        }
        user.setOnboardingCompleted(true);
        userRepository.save(user);
    }

    private void writeOtpToProjectFile(String code) {
        try {
            java.io.File file = new java.io.File("otp.txt");
            java.nio.file.Files.writeString(file.toPath(), code);
            System.out.println("[DEV] OTP written to: " + file.getAbsolutePath());
            
            java.io.File parentFile = new java.io.File("../otp.txt");
            if (new java.io.File("../Postman Collections").exists()) {
                java.nio.file.Files.writeString(parentFile.toPath(), code);
                System.out.println("[DEV] OTP also written to project root directory: " + parentFile.getAbsolutePath());
            }
        } catch (Exception e) {
            System.err.println("Failed to write OTP to project file: " + e.getMessage());
        }
    }
}

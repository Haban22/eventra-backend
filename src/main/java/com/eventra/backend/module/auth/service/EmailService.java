package com.eventra.backend.module.auth.service;

import com.eventra.backend.module.auth.config.AppProperties;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
public class EmailService {
    private final JavaMailSender mailSender;
    private final AppProperties properties;

    public EmailService(JavaMailSender mailSender, AppProperties properties) {
        this.mailSender = mailSender;
        this.properties = properties;
    }

    public void sendVerificationEmail(String email, String rawToken) {
        // Path param, not query param — matches the frontend's /verify-email/:token route
        send(email, "Verify your Eventra email", properties.frontendUrl() + "/verify-email/" + rawToken);
    }

    public void sendPasswordResetEmail(String email, String rawToken) {
        // Path param, not query param — matches the frontend's /reset-password/:token route
        send(email, "Reset your Eventra password", properties.frontendUrl() + "/reset-password/" + rawToken);
    }

    public void sendOtpEmail(String email, String otp) {
        send(email, "Your Eventra One-Time Password", "Your OTP code is: " + otp + ". It is valid for 5 minutes.");
    }

    private void send(String to, String subject, String body) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom(properties.mailFrom());
        message.setTo(to);
        message.setSubject(subject);
        message.setText(body);
        mailSender.send(message);
    }
}

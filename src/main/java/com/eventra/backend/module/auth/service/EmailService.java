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
        send(email, "Verify your Eventra email", properties.frontendUrl() + "/verify-email?token=" + rawToken);
    }

    public void sendPasswordResetEmail(String email, String rawToken) {
        send(email, "Reset your Eventra password", properties.frontendUrl() + "/reset-password?token=" + rawToken);
    }

    public void sendOtpEmail(String email, String code, String purpose) {
        String subject = "ADMIN_LOGIN".equals(purpose)
                ? "Your Eventra Admin Sign-in Code"
                : "Your Eventra Verification Code";
        String body = "Your one-time verification code is: " + code + "\n\nThis code expires in 10 minutes. Do not share it with anyone.";
        send(email, subject, body);
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

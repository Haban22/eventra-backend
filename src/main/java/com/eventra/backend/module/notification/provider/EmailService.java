package com.eventra.backend.module.notification.provider;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.mail.MailException;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

/**
 * Email delivery provider backed by Spring's {@link JavaMailSender}.
 *
 * <p>All sends are executed asynchronously via {@code @Async} to avoid
 * blocking the calling thread on SMTP I/O.</p>
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class EmailService {

    private final JavaMailSender mailSender;

    /**
     * Sends a plain-text email notification asynchronously.
     *
     * @param toEmail   recipient email address
     * @param subject   email subject line
     * @param body      email plain-text body
     */
    @Async
    public void send(String toEmail, String subject, String body) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setTo(toEmail);
            message.setSubject(subject);
            message.setText(body);
            mailSender.send(message);
            log.info("Email sent to {}", toEmail);
        } catch (MailException ex) {
            log.error("Failed to send email to {}: {}", toEmail, ex.getMessage());
        }
    }
}

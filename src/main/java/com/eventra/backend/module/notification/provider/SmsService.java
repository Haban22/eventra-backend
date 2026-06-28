package com.eventra.backend.module.notification.provider;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * SMS delivery provider.
 *
 * <p>Stub implementation — integrate with Twilio, AWS SNS, or another SMS
 * gateway when SMS support is required.</p>
 */
@Slf4j
@Service
public class SmsService {

    /**
     * Sends an SMS notification to a phone number.
     *
     * @param phoneNumber  recipient phone number (E.164 format, e.g., +1234567890)
     * @param message      SMS message body
     */
    public void send(String phoneNumber, String message) {
        // TODO: Integrate with Twilio / AWS SNS SMS provider
        log.info("SMS notification (stub) — to: {}", phoneNumber);
    }
}

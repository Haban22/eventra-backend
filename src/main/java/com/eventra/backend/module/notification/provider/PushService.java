package com.eventra.backend.module.notification.provider;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * Push notification delivery provider.
 *
 * <p>Stub implementation — integrate with FCM, APNs, or a third-party push
 * service (e.g., OneSignal, Expo) when mobile client support is added.</p>
 */
@Slf4j
@Service
public class PushService {

    /**
     * Sends a push notification to a device token.
     *
     * @param deviceToken  target device token
     * @param title        notification title
     * @param body         notification body
     */
    public void send(String deviceToken, String title, String body) {
        // TODO: Integrate with FCM / APNs push provider
        log.info("Push notification (stub) — token: {}, title: {}", deviceToken, title);
    }
}

package com.eventra.backend.common.websocket;

import com.eventra.backend.module.auth.security.JwtUtil;
import io.jsonwebtoken.Claims;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.messaging.support.MessageHeaderAccessor;
import org.springframework.stereotype.Component;

import java.security.Principal;
import java.util.UUID;

// Validates the JWT on the STOMP CONNECT frame (sent by the client as a STOMP header,
// not an HTTP header — the SockJS handshake itself is unauthenticated, see
// WebSocketConfig). Rejects the connection outright if the token is missing/invalid,
// otherwise attaches a Principal whose getName() is the user's id, so
// SimpMessagingTemplate.convertAndSendToUser(userId, ...) routes correctly.
@Component
public class StompAuthChannelInterceptor implements ChannelInterceptor {
    private final JwtUtil jwtUtil;

    public StompAuthChannelInterceptor(JwtUtil jwtUtil) {
        this.jwtUtil = jwtUtil;
    }

    @Override
    public Message<?> preSend(Message<?> message, MessageChannel channel) {
        StompHeaderAccessor accessor = MessageHeaderAccessor.getAccessor(message, StompHeaderAccessor.class);
        if (accessor != null && StompCommand.CONNECT.equals(accessor.getCommand())) {
            String authHeader = accessor.getFirstNativeHeader("Authorization");
            if (authHeader == null) {
                authHeader = accessor.getFirstNativeHeader("authorization");
            }
            String token = authHeader != null && authHeader.startsWith("Bearer ") ? authHeader.substring(7) : authHeader;

            if (token == null || token.isBlank()) {
                throw new org.springframework.messaging.MessagingException("Missing WebSocket auth token");
            }

            try {
                Claims claims = jwtUtil.validateAndParse(token);
                String jti = claims.getId();
                if (jwtUtil.isBlacklisted(jti)) {
                    throw new org.springframework.messaging.MessagingException("Token has been revoked");
                }
                UUID userId = UUID.fromString(claims.getSubject());
                accessor.setUser((Principal) () -> userId.toString());
            } catch (org.springframework.messaging.MessagingException e) {
                throw e;
            } catch (Exception e) {
                throw new org.springframework.messaging.MessagingException("Invalid WebSocket auth token", e);
            }
        }
        return message;
    }
}

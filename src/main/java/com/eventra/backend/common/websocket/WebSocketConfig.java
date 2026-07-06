package com.eventra.backend.common.websocket;

import com.eventra.backend.module.auth.config.CorsProperties;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.ChannelRegistration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;

// REST persists (source of truth); after each write, SimpMessagingTemplate pushes a
// notification to connected clients — there is no STOMP write path, clients only
// subscribe. Auth happens on the STOMP CONNECT frame (see StompAuthChannelInterceptor),
// not on the SockJS HTTP handshake, so /ws/** is left out of the JWT-required paths in
// SecurityConfig.
@Configuration
@EnableWebSocketMessageBroker
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {
    private final CorsProperties corsProperties;
    private final StompAuthChannelInterceptor authChannelInterceptor;

    @Autowired
    public WebSocketConfig(CorsProperties corsProperties, StompAuthChannelInterceptor authChannelInterceptor) {
        this.corsProperties = corsProperties;
        this.authChannelInterceptor = authChannelInterceptor;
    }

    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        registry.addEndpoint("/ws")
                .setAllowedOrigins(corsProperties.allowedOrigins().split(","))
                .withSockJS();
    }

    @Override
    public void configureMessageBroker(MessageBrokerRegistry registry) {
        registry.enableSimpleBroker("/topic", "/queue");
        registry.setApplicationDestinationPrefixes("/app");
        registry.setUserDestinationPrefix("/user");
    }

    @Override
    public void configureClientInboundChannel(ChannelRegistration registration) {
        registration.interceptors(authChannelInterceptor);
    }
}

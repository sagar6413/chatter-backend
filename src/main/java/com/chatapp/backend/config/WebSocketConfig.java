package com.chatapp.backend.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.event.EventListener;
import org.springframework.messaging.converter.DefaultContentTypeResolver;
import org.springframework.messaging.converter.MappingJackson2MessageConverter;
import org.springframework.messaging.converter.MessageConverter;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;
import org.springframework.web.socket.config.annotation.WebSocketTransportRegistration;
import org.springframework.web.socket.handler.WebSocketHandlerDecorator;
import org.springframework.web.socket.messaging.SessionConnectedEvent;
import org.springframework.web.socket.messaging.SessionDisconnectEvent;
import org.springframework.web.socket.server.standard.ServletServerContainerFactoryBean;

import java.util.List;

import static org.springframework.util.MimeTypeUtils.APPLICATION_JSON;

@Configuration
@EnableWebSocketMessageBroker
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {

    @Override
    public void configureMessageBroker(MessageBrokerRegistry registry) {
        registry.enableSimpleBroker("/topic", "/queue", "/user")// to use simple broker
                .setHeartbeatValue(new long[]{10000, 10000});
        // to use RabbitMq
        // registry.enableStompBrokerRelay("/topic").setRelayHost("localhost").setRelayPort(61613).setClientLogin("guest").setClientPasscode("guest");
        registry.setApplicationDestinationPrefixes("/app");
        registry.setUserDestinationPrefix("/user");
    }

    @Override
    public void configureWebSocketTransport(WebSocketTransportRegistration registry) {
        registry.addDecoratorFactory(handler -> new WebSocketHandlerDecorator(handler) {
            @Override
            public void handleTransportError(WebSocketSession session, Throwable exception) throws Exception {
                super.handleTransportError(session, exception);
                // Log error or notify monitoring system
            }
        });
    }

    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        registry
                .addEndpoint("/ws")
                .setAllowedOrigins("http://localhost:3000", "https://production-domain.com")
                .withSockJS()
                // Configure SockJS settings for better reliability
                .setStreamBytesLimit(512 * 1024)    // 512KB
                .setHttpMessageCacheSize(1000)
                .setDisconnectDelay(30 * 1000);     // 30 seconds
    }

    // Configure message size limits and timeouts
    @Bean
    public ServletServerContainerFactoryBean createWebSocketContainer() {
        ServletServerContainerFactoryBean container = new ServletServerContainerFactoryBean();
        container.setMaxTextMessageBufferSize(64 * 1024);    // 64KB
        container.setMaxBinaryMessageBufferSize(1024 * 1024); // 1MB
        // Set maximum timeout (default is 10 minutes)
        container.setMaxSessionIdleTimeout(600000L);
        return container;
    }

    @Override
    public boolean configureMessageConverters(List<MessageConverter> messageConverters) {

        DefaultContentTypeResolver resolver = new DefaultContentTypeResolver();
        resolver.setDefaultMimeType(APPLICATION_JSON);
        MappingJackson2MessageConverter converter = new MappingJackson2MessageConverter();
        converter.setObjectMapper(new ObjectMapper());
        converter.setContentTypeResolver(resolver);
        messageConverters.add(converter);

        return false;
    }

    @EventListener
    public void handleSessionConnected(SessionConnectedEvent event) {
        // Handle new connection
    }

    @EventListener
    public void handleSessionDisconnect(SessionDisconnectEvent event) {
        // Clean up on disconnect
    }

}

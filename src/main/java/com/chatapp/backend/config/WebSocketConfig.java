package com.chatapp.backend.config;

import com.chatapp.backend.config.jwt.JwtService;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.MessageDeliveryException;
import org.springframework.messaging.converter.DefaultContentTypeResolver;
import org.springframework.messaging.converter.MappingJackson2MessageConverter;
import org.springframework.messaging.converter.MessageConverter;
import org.springframework.messaging.simp.config.ChannelRegistration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.messaging.support.MessageHeaderAccessor;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;
import org.springframework.web.socket.config.annotation.WebSocketTransportRegistration;
import org.springframework.web.socket.handler.WebSocketHandlerDecorator;
import org.springframework.web.socket.server.standard.ServletServerContainerFactoryBean;

import java.util.List;

import static org.springframework.util.MimeTypeUtils.APPLICATION_JSON;

@Configuration
@EnableWebSocketMessageBroker
@EnableAsync
@EnableScheduling
@Slf4j
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {

    private final JwtService jwtService;

    public WebSocketConfig(JwtService jwtService) {
        this.jwtService = jwtService;
    }

    @Override
    public void configureMessageBroker(MessageBrokerRegistry registry) {
        // Use a simple in-memory message broker for development
        // Enable a full-featured message broker like RabbitMQ in production
        registry.enableSimpleBroker("/topic", "/queue")
                .setTaskScheduler(taskScheduler()) // Add TaskScheduler for heartbeats
                .setHeartbeatValue(new long[]{10000, 10000}); // Maintain heartbeats for connection health
        // To use RabbitMQ, uncomment the following lines and configure your RabbitMQ connection:
        // registry.enableStompBrokerRelay("/topic")
        //         .setRelayHost("your-rabbitmq-host")
        //         .setRelayPort(61613)
        //         .setClientLogin("your-rabbitmq-username")
        //         .setClientPasscode("your-rabbitmq-password");
        registry.setApplicationDestinationPrefixes("/app");
        registry.setUserDestinationPrefix("/user");
        log.info("Message broker configured with application destination prefix '/app' and user destination prefix '/user'.");
    }

    @Override
    public void configureWebSocketTransport(WebSocketTransportRegistration registry) {
        registry.setSendTimeLimit(15 * 1000)
                .setSendBufferSizeLimit(512 * 1024)
                .setMessageSizeLimit(128 * 1024)
                .addDecoratorFactory(handler -> new WebSocketHandlerDecorator(handler) {

                    @Override
                    public void handleTransportError(WebSocketSession session, Throwable exception) throws Exception {
                        super.handleTransportError(session, exception);
                        log.error("WebSocket transport error occurred in session: {}", session.getId(), exception);
                        // Consider implementing a mechanism to notify the user or retry connection
                    }
                });
        log.info("WebSocket transport configured with custom error handling.");
    }

    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        registry.addEndpoint("/ws")
                .setAllowedOrigins("http://localhost:3000", "https://production-domain.com") // Adjust allowed origins for security
                .withSockJS() // Enable SockJS for fallback options in older browsers
                .setStreamBytesLimit(512 * 1024) // Configure stream bytes limit for SockJS
                .setHttpMessageCacheSize(1000)
                .setDisconnectDelay(30 * 1000); // Configure disconnect delay to allow for reconnections
        log.info("STOMP endpoint '/ws' registered with SockJS support and allowed origins: http://localhost:3000, https://production-domain.com");
    }

    @Bean
    public ServletServerContainerFactoryBean createWebSocketContainer() {
        ServletServerContainerFactoryBean container = new ServletServerContainerFactoryBean();
        container.setMaxTextMessageBufferSize(64 * 1024); // Limit text message buffer size
        container.setMaxBinaryMessageBufferSize(1024 * 1024); // Limit binary message buffer size
        container.setMaxSessionIdleTimeout(15 * 60 * 1000L); // Increased idle timeout for longer sessions, adjust as needed
        log.info("WebSocket container configured with message buffer sizes and session idle timeout.");
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
        log.info("Configured Jackson message converter for JSON serialization/deserialization.");
        return false;
    }

    @Override
    public void configureClientInboundChannel(ChannelRegistration registration) {
        registration.interceptors(new ChannelInterceptor() {
            @Override
            public Message<?> preSend(Message<?> message, MessageChannel channel) {
                StompHeaderAccessor accessor = MessageHeaderAccessor.getAccessor(message, StompHeaderAccessor.class);

                if (accessor != null && StompCommand.CONNECT.equals(accessor.getCommand())) {
                    // Extract JWT token from headers
                    List<String> authHeaders = accessor.getNativeHeader("Authorization");
                    if (authHeaders != null && !authHeaders.isEmpty()) {
                        String bearerToken = authHeaders.get(0);
                        if (bearerToken != null && bearerToken.startsWith("Bearer ")) {
                            String jwt = bearerToken.substring(7);

                            try {
                                // Validate JWT and extract username
                                String username = jwtService.validateTokenAndGetUsername(jwt);

                                // Set authenticated user in WebSocket connection
                                accessor.setUser(() -> username);

                                log.info("Authenticated WebSocket connection for user: {}", username);
                            } catch (Exception e) {
                                log.error("Invalid JWT token in WebSocket connection", e);
                                // You might want to throw an exception here to prevent connection
                                throw new MessageDeliveryException("Invalid token");
                            }
                        }
                    }
                }
                return message;
            }
        });
    }

    @Bean
    public TaskScheduler taskScheduler() {
        ThreadPoolTaskScheduler scheduler = new ThreadPoolTaskScheduler();
        scheduler.setPoolSize(2);
        scheduler.setThreadNamePrefix("websocket-tasks-");
        scheduler.initialize();
        return scheduler;
    }
}
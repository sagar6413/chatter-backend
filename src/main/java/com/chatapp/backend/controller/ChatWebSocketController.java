package com.chatapp.backend.controller;

import com.chatapp.backend.dto.request.MessageRequest;
import com.chatapp.backend.entity.User;
import com.chatapp.backend.service.ConversationService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;

@Controller
@RequiredArgsConstructor
@Slf4j
public class ChatWebSocketController {
    private final ConversationService conversationService;
    private final SimpMessagingTemplate messagingTemplate;

    @MessageMapping("/chat.sendPrivateMessage")
    public void handlePrivateMessage(@Payload @Valid MessageRequest messageRequest) {
        User user = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        log.info("User {}: Received private message: {}", user.getUsername(), messageRequest);

        conversationService.processPrivateMessage(messageRequest).thenAccept(response -> {
            // Send the full response after processing is complete
            messagingTemplate.convertAndSend("/queue/chat/" + response.conversationId(), response);
            log.info("User {}: Sent private message response, conversationId: {}", user.getUsername(), response.conversationId());
        });
    }

    @MessageMapping("/chat.sendGroupMessage/")
    public void handleGroupMessage( @Payload @Valid MessageRequest messageRequest) {
        String username = "LOGGED_IN_USER";//TODO : Replace with authenticated user from spring context once implemented Spring security
        log.info("User {}: Received group message for chat {}: {}", username, messageRequest.conversationId(), messageRequest);

        // Process the message asynchronously
        conversationService.processGroupMessage(messageRequest.conversationId(), messageRequest).thenAccept(response -> {
            // Send the full response after processing is complete
            messagingTemplate.convertAndSend("/topic/chat/" + messageRequest.conversationId(), response);
            log.info("User {}: Sent group message response to /topic/chat/{}, response: {}", username, messageRequest.conversationId(), response);
        });
    }
}
/**
 * public class WebSocketController {
 * <p>
 * private final SimpMessagingTemplate messagingTemplate;
 * private final ChatService chatService;
 *
 * @MessageMapping("/chat.sendMessage") public void sendMessage(@Payload MessageRequest message) {
 * MessageResponse response = chatService.processMessage(message);
 * messagingTemplate.convertAndSend(
 * "/topic/conversations/" + response.conversationId(),
 * response
 * );
 * }
 * @MessageMapping("/chat.startTyping") public void startTyping(@DestinationVariable Long conversationId,
 * @Payload String username) {
 * messagingTemplate.convertAndSend(
 * "/topic/conversations/" + conversationId + "/typing",
 * Map.of("username", username, "isTyping", true)
 * );
 * }
 * @MessageMapping("/message.markDelivered") public void handleMarkDelivered(@Payload MessageStatusUpdateRequest updateRequest, Principal principal) {
 * String username = principal.getName();
 * log.info("User {}: Received message delivered update: {}", username, updateRequest);
 * conversationService.markMessageDelivered(updateRequest.getMessageId(), username);
 * }
 * @MessageMapping("/message.markRead") public void handleMarkRead(@Payload MessageStatusUpdateRequest updateRequest, Principal principal) {
 * String username = principal.getName();
 * log.info("User {}: Received message read update: {}", username, updateRequest);
 * conversationService.markMessageRead(updateRequest.getMessageId(), username);
 * }
 * @MessageMapping("/chat.stopTyping") public void stopTyping(@DestinationVariable Long conversationId,
 * @Payload String username) {
 * messagingTemplate.convertAndSend(
 * "/topic/conversations/" + conversationId + "/typing",
 * Map.of("username", username, "isTyping", false)
 * );
 * }
 * @MessageMapping("/chat.markAsRead") public void markAsRead(@Payload Map<String, Object> payload) {
 * Long conversationId = (Long) payload.get("conversationId");
 * Long messageId = (Long) payload.get("messageId");
 * <p>
 * DeliveryStatusSummary summary = chatService.markAsRead(conversationId, messageId);
 * messagingTemplate.convertAndSend(
 * "/topic/conversations
 */
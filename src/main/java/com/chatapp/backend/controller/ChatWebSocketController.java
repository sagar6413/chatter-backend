package com.chatapp.backend.controller;

import com.chatapp.backend.dto.MessageDTO;
import com.chatapp.backend.dto.MessageResponseDTO;
import com.chatapp.backend.service.ChatService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.messaging.simp.annotation.SendToUser;
import org.springframework.stereotype.Controller;

@Controller
@RequiredArgsConstructor
@Slf4j
public class ChatWebSocketController {
    private final ChatService chatService;

    @MessageMapping("/chat.sendPrivateMessage")
    public void handlePrivateMessage(@Payload MessageDTO messageDTO) {
        log.info("Received private message to user {}", messageDTO.receiverId());
        chatService.sendPrivateMessage(messageDTO);
    }

    @MessageMapping("/chat.sendGroupMessage/{chatId}")
    @SendTo("/topic/chat/{chatId}")
    public void handleGroupMessage(
            @DestinationVariable Long chatId,
            @Payload MessageDTO messageDTO) {
        log.info("Received group message for chat {}: {}", chatId, messageDTO);
        chatService.saveAndProcessGroupMessage(chatId, messageDTO);
    }
}
package com.chatapp.backend.controller;

import com.chatapp.backend.dto.MessageDTO;
import com.chatapp.backend.service.ContactService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.stereotype.Controller;

@Controller
@RequiredArgsConstructor
@Slf4j
public class ContactWebSocketController {
    private final ContactService contactService;

    @MessageMapping("/chat.sendPrivateMessage")
    public void handlePrivateMessage(@Payload MessageDTO messageDTO) {
        log.info("Received private message to user {}", messageDTO.receiverId());
        contactService.sendPrivateMessage(messageDTO);
    }

    @MessageMapping("/chat.sendGroupMessage/{chatId}")
    @SendTo("/topic/chat/{chatId}")
    public void handleGroupMessage(
            @DestinationVariable Long chatId,
            @Payload MessageDTO messageDTO) {
        log.info("Received group message for chat {}: {}", chatId, messageDTO);
        contactService.saveAndProcessGroupMessage(chatId, messageDTO);
    }
}
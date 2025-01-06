package com.chatapp.backend.controller;

import com.chatapp.backend.dto.*;
import com.chatapp.backend.service.ChatService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/chats")
@RequiredArgsConstructor
@Slf4j
public class ChatController {
    private final ChatService chatService;

    @PostMapping
    public ResponseEntity<ChatDTO> createChat(@RequestBody CreateChatRequestDTO request) {
        log.info("Creating new chat: {}", request);
        return ResponseEntity.ok(chatService.createChat(request));
    }

    @GetMapping("/user/{userId}")
    public ResponseEntity<List<ChatDTO>> getUserChats(
            @PathVariable Long userId,
            @RequestParam(required = false) Boolean unreadOnly) {
        log.info("Fetching chats for user {} with unreadOnly: {}", userId, unreadOnly);
        return ResponseEntity.ok(chatService.getUserChats(userId, unreadOnly));
    }

    @GetMapping("/{chatId}/messages")
    public ResponseEntity<List<ChatMessageDTO>> getChatHistory(@PathVariable Long chatId) {
        log.info("Fetching chat history for chat: {}", chatId);
        return ResponseEntity.ok(chatService.getChatHistory(chatId));
    }

    @PostMapping("/{chatId}/read")
    public ResponseEntity<Void> markMessagesAsRead(
            @PathVariable Long chatId,
            @RequestBody MarkReadRequestDTO request) {
        log.info("Marking messages as read in chat {} for user {}", chatId, request.userId());
        chatService.markMessagesAsRead(chatId, request.lastReadMessageId(), request.userId());
        return ResponseEntity.ok().build();
    }
}

package com.chatapp.backend.controller;

import com.chatapp.backend.dto.request.GroupRequest;
import com.chatapp.backend.dto.request.GroupSettingsRequest;
import com.chatapp.backend.dto.response.*;
import com.chatapp.backend.entity.enums.MessageStatus;
import com.chatapp.backend.service.ConversationService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Set;

/**
 * Controller class for managing conversations in the chat application.
 */
@RestController
@RequestMapping("/api/chats")
@RequiredArgsConstructor
@Slf4j
public class ConversationController {

    private final ConversationService conversationService;


    @GetMapping("/conversations")
    public ResponseEntity<Page<ConversationResponse>> getUserConversations(Pageable pageable) {
        return ResponseEntity.ok(conversationService.getUserConversations(pageable));
    }

    @GetMapping("/{conversationId}")
    public ResponseEntity<ConversationResponse> getConversation(@PathVariable Long conversationId) {
        return ResponseEntity.ok(conversationService.getConversation(conversationId));
    }

    @PostMapping("/group")
    public ResponseEntity<GroupConversationResponse> createGroupConversation(@Valid @RequestBody GroupRequest request) {
        log.info("Creating a new group conversation");
        return ResponseEntity.ok(conversationService.createGroupConversation(request));
    }

    @PutMapping("/{groupId}/settings")
    public ResponseEntity<GroupSettingsResponse> updateGroupSettings(@PathVariable Long groupId, @Valid @RequestBody GroupSettingsRequest request) {
        log.info("Updating settings for group with ID: {}", groupId);
        return ResponseEntity.ok(conversationService.updateGroupSettings(groupId, request));
    }

    @PostMapping("/{groupId}/participants")
    public ResponseEntity<?> addParticipants(@PathVariable Long groupId, @RequestBody Set<String> participantIds) {
        log.info("Adding participants to group with ID: {}", groupId);
        return ResponseEntity.ok(conversationService.addParticipants(groupId, participantIds));
    }

    @DeleteMapping("/{groupId}/participants/{username}")
    public ResponseEntity<Set<UserResponse>> removeParticipant(@PathVariable Long groupId, @PathVariable String username) {
        log.info("Removing participant {} from group with ID: {}", username, groupId);
        return ResponseEntity.ok(conversationService.removeParticipant(groupId, username));
    }

    @PostMapping("/private/{username}")
    public ResponseEntity<PrivateConversationResponse> createPrivateConversation(@PathVariable String username) {
        log.info("Creating private conversation for user: {}", username);
        return ResponseEntity.ok(conversationService.createPrivateConversation(username));
    }

    @GetMapping("/{username}/{conversationId}/messages")
    public ResponseEntity<?> getMessages(@PathVariable String username, @PathVariable Long conversationId, Pageable pageable) {
        log.info("Fetching messages for user: {}, conversation ID: {}", username, conversationId);
        return ResponseEntity.ok(conversationService.getMessages(username, conversationId, pageable));
    }

    @PutMapping("/{username}/messages/{messageId}/status")
    public ResponseEntity<MessageDeliveryStatusResponse> changeMessageStatus(@PathVariable String username, @PathVariable Long messageId, @RequestBody MessageStatus status) {
        log.info("Changing status of message ID: {}", messageId);
        return ResponseEntity.ok(conversationService.changeMessageStatus(username, messageId, status));
    }

    @DeleteMapping("/{conversationId}")
    public ResponseEntity<?> deleteConversation(@PathVariable Long conversationId) {
        log.info("Deleting conversation with ID: {}", conversationId);
        conversationService.deleteConversation(conversationId);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/private-chats")
    public ResponseEntity<Page<PrivateConversationResponse>> getUserPrivateChats(Pageable pageable) {
        log.info("Fetching private chats");
        return ResponseEntity.ok(conversationService.getUserPrivateChats( pageable));
    }

    @GetMapping("/group-chats")
    public ResponseEntity<Page<GroupConversationResponse>> getUserGroupChats( Pageable pageable) {
        log.info("Fetching group chats");
        return ResponseEntity.ok(conversationService.getUserGroupChats( pageable));
    }
}

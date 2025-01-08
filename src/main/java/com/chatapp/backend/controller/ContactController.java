package com.chatapp.backend.controller;

import com.chatapp.backend.dto.*;
import com.chatapp.backend.service.ContactService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/contacts")
@RequiredArgsConstructor
@Slf4j
public class ContactController {
    private final ContactService contactService;

    @PostMapping
    public ResponseEntity<ContactDTO> createContact(@RequestBody CreateContactRequestDTO request) {
        log.info("Creating new contact: {}", request);
        return ResponseEntity.ok(contactService.createContact(request));
    }

    @GetMapping("/user/{userId}")
    public ResponseEntity<List<ContactDTO>> getUserContacts(
            @PathVariable Long userId,
            @RequestParam(required = false) Boolean unreadOnly) {
        log.info("Fetching contacts for user {} with unreadOnly: {}", userId, unreadOnly);
        return ResponseEntity.ok(contactService.getUserContacts(userId, unreadOnly));
    }

    @GetMapping("/{contactId}/messages")
    public ResponseEntity<List<ContactMessageDTO>> getContactHistory(@PathVariable Long contactId) {
        log.info("Fetching contact history for contact: {}", contactId);
        return ResponseEntity.ok(contactService.getContactHistory(contactId));
    }

    @PostMapping("/{contactId}/read")
    public ResponseEntity<Void> markMessagesAsRead(
            @PathVariable Long contactId,
            @RequestBody MarkReadRequestDTO request) {
        log.info("Marking messages as read in contact {} for user {}", contactId, request.userId());
        contactService.markMessagesAsRead(contactId, request.lastReadMessageId(), request.userId());
        return ResponseEntity.ok().build();
    }
}

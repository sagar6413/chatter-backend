package com.chatapp.backend.service;


import com.chatapp.backend.dto.*;

import java.util.List;

public interface ContactService {
    ContactDTO createContact(CreateContactRequestDTO request);
    List<ContactDTO> getUserContacts(Long userId, Boolean unreadOnly);
    List<ContactMessageDTO> getContactHistory(Long contactId);
    void markMessagesAsRead(Long contactId, Long lastReadMessageId, Long userId);
    void sendPrivateMessage(MessageDTO messageDTO);
    void saveAndProcessGroupMessage(Long contactId, MessageDTO messageDTO);
}

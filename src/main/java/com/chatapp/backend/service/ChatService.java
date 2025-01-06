package com.chatapp.backend.service;


import com.chatapp.backend.dto.*;
import com.chatapp.backend.entity.Message;

import java.util.List;

public interface ChatService {
    ChatDTO createChat(CreateChatRequestDTO request);
    List<ChatDTO> getUserChats(Long userId, Boolean unreadOnly);
    List<ChatMessageDTO> getChatHistory(Long chatId);
    void markMessagesAsRead(Long chatId, Long lastReadMessageId, Long userId);
    void sendPrivateMessage(MessageDTO messageDTO);
    void saveAndProcessGroupMessage(Long chatId, MessageDTO messageDTO);
}

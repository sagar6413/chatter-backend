package com.chatapp.backend.dto;

import com.chatapp.backend.entity.enums.ConversationType;
import lombok.Builder;

import java.util.List;

@Builder
public record ContactDTO(Long id, String name, ConversationType type, List<UserDTO> participants, List<MessageDTO> latestMessage, Long unreadCount) {
}

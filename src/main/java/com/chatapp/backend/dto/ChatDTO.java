package com.chatapp.backend.dto;

import com.chatapp.backend.entity.ChatType;
import lombok.Builder;

import java.util.List;

@Builder
public record ChatDTO(Long id, String name, ChatType type, List<UserDTO> participants, List<MessageDTO> latestMessage, Long unreadCount) {
}

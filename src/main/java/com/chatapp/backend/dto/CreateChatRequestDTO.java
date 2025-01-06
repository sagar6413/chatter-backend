package com.chatapp.backend.dto;

import com.chatapp.backend.entity.ChatType;

import java.util.List;

public record CreateChatRequestDTO(String name, List<Long> participantIds, ChatType type) {
}


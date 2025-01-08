package com.chatapp.backend.dto;

import com.chatapp.backend.entity.enums.ConversationType;

import java.util.List;

public record CreateContactRequestDTO(String name, List<Long> participantIds, ConversationType type) {
}


package com.chatapp.backend.dto;

public record MarkReadRequestDTO(Long userId, Long lastReadMessageId) {
}

package com.chatapp.backend.dto;

import lombok.Builder;

import java.time.Instant;

@Builder
public record ContactMessageDTO(Long id, Long chatId, Long senderId, String content,
                                Instant createdAt) {
}

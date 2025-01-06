package com.chatapp.backend.dto;

import lombok.*;

import java.time.Instant;

@Builder
public record MessageResponseDTO (
        Long id,
        Long chatId,
        Long senderId,
        String content,
        Instant createdAt
) {}

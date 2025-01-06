package com.chatapp.backend.dto;

import lombok.*;

import java.time.Instant;

@Builder
public record MessageDTO (
     Long id,
     Long chatId,
     Long senderId,
     Long receiverId,
     String content,
     Instant createdAt
    // You might include media URLs or IDs here if needed in the DTO
){}


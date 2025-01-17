package com.chatapp.backend.dto.response;

import lombok.Builder;

import java.time.Instant;

@Builder
public record ConversationResponse(Long conversationId,
                                   UserResponse contact,
                                   MessageResponse lastMessage,
                                   Instant createdAt,
                                   Instant updatedAt) {
}

package com.chatapp.backend.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Builder;

import java.time.Instant;

@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public record PrivateConversationResponse(Long conversationId,
                                          UserResponse contact,
                                          MessageResponse lastMessage,
                                          Instant createdAt,
                                          Instant updatedAt) {
}

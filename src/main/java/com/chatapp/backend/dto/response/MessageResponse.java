package com.chatapp.backend.dto.response;

import com.chatapp.backend.entity.enums.MessageType;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Builder;

import java.time.Instant;
import java.util.Set;

@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public record MessageResponse(Long id,
                              Long conversationId,
                              String senderUsername,
                              String senderAvatarUrl,
                              String senderDisplayName,
                              String content,
                              Set<MediaResponse> mediaItems,
                              MessageType type,
                              Set<ReactionResponse> reactions,
                              Instant createdAt,
                              Instant editedAt,
                              Set<MessageDeliveryStatusResponse> deliveryStatus) {
}
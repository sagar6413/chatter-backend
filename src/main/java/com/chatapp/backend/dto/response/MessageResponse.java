package com.chatapp.backend.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import java.util.Set;
import lombok.Builder;
import java.time.Instant;

@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public record MessageResponse (
     Long id,
     Long conversationId,
     String senderuserName,
     String senderAvatarUrl,
     String senderDisplayName,
     String content,
     Set<MediaResponse> mediaItems,
     MessageType type,
     MessageStatus status,
     Set<ReactionResponse> reactions,
     Instant createdAt,
     Instant editedAt,
     DeliveryStatusSummary deliveryStatus,
){}
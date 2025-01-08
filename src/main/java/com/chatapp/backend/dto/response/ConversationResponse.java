package com.chatapp.backend.dto.response;

import com.chatapp.backend.entity.enums.ConversationType;
import com.fasterxml.jackson.annotation.JsonInclude;

import lombok.Builder;
import java.util.Set;
import java.time.Instant;

@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public record ConversationResponse (
     Long id,
     ConversationType type,
     Set<UserResponse> participants,
     int participantCount,
     MessageResponse lastMessage,
     Instant createdAt,
     Instant updatedAt,
     String groupName,
     String groupDescription,
     String creatorName,
     String creatorAvatarUrl,
     String creatorUserName,
     boolean onlyAdminsCanSend,
     Integer messageRetentionDays,
     Integer maxMembers,
     boolean isGroupPublic
){}

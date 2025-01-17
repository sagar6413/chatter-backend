package com.chatapp.backend.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Builder;

import java.time.Instant;
import java.util.Set;

@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public record GroupConversationResponse(Long conversationId,
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
                                        boolean isGroupPublic) {
}


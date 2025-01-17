package com.chatapp.backend.dto.request;

import com.chatapp.backend.entity.enums.MessageType;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Builder;

import java.util.Set;

@Builder
public record MessageRequest(@NotNull(message = "Conversation ID is required") Long conversationId,

                             @Size(max = 4000, message = "Message content cannot exceed 4000 characters") String content,

                             Set<Long> mediaIds,
                             // Optional - for messages with attachments

                             MessageType type,
                             // Default to TEXT if not specified

                             @Size(max = 100, message = "Reply thread depth cannot exceed 100") Long replyToMessageId
                             // Optional - for threaded replies
) {
}
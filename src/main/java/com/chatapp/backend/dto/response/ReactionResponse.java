package com.chatapp.backend.dto.response;

import com.chatapp.backend.entity.enums.ReactionType;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Builder;

@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public record ReactionResponse(Long id, String username, String displayName, String avatarUrl, ReactionType type) {
}
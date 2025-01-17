package com.chatapp.backend.dto.response;

import com.chatapp.backend.entity.enums.UserStatus;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Builder;

import java.time.Instant;


@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public record UserResponse(Long id,
                           String username,
                           String displayName,
                           UserStatus status,
                           Instant lastSeenAt,
                           UserPreferenceResponse preferences,
                           Instant createdAt) {
}
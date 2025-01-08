package com.chatapp.backend.dto.response;

import java.time.Instant;

import com.chatapp.backend.entity.enums.UserStatus;
import com.fasterxml.jackson.annotation.JsonInclude;

import lombok.Builder;


@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public record UserResponse (
    Long id,
    String username,
    String displayName,
    UserStatus status,
    Instant lastSeenAt,
    UserPreferencesResponse preferences,
    Instant createdAt
){}
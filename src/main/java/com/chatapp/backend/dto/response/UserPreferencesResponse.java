package com.chatapp.backend.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;

import lombok.Builder;

@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public record UserPreferencesResponse (
     boolean notificationEnabled,
     String theme,
){}
package com.chatapp.backend.dto.request;

import lombok.Builder;

@Builder
public record UserRequest(String username, String displayName, UserPreferenceRequest preferences) {
}

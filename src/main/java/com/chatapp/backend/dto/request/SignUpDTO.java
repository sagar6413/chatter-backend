package com.chatapp.backend.dto.request;

import lombok.Builder;

@Builder
public record SignUpDTO(String username, String displayName, String password) {
}

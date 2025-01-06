package com.chatapp.backend.dto.request;

import lombok.Builder;

@Builder
public record SignInDTO(String username, String password) {
}

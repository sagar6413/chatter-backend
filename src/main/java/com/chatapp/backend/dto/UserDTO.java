package com.chatapp.backend.dto;

import lombok.Builder;

@Builder
public record UserDTO(String username, String displayName) {
}

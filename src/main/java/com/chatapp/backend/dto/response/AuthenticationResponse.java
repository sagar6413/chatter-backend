package com.chatapp.backend.dto.response;

import lombok.Builder;

@Builder
public record AuthenticationResponse(String refreshToken, String accessToken) {
}

package com.chatapp.backend.dto;

import lombok.Builder;

import java.time.LocalDateTime;

@Builder
public record ReadReceiptDto(Long id, Long userId, LocalDateTime now) {
}

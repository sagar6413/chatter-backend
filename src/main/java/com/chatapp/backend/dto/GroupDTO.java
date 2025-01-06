package com.chatapp.backend.dto;

import lombok.Builder;

import java.time.Instant;
import java.util.List;

@Builder
public record GroupDTO(Long id, String name, String description, List<UserDTO> members, Instant createdAt) {
}

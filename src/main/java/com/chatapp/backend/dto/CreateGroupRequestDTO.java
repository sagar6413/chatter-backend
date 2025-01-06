package com.chatapp.backend.dto;

import java.util.List;

public record CreateGroupRequestDTO(String name, String description, Long creatorId, List<Long> memberIds) {
}

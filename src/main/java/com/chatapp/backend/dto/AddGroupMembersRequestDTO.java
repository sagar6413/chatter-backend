package com.chatapp.backend.dto;

import lombok.Builder;

import java.util.List;

@Builder
public record AddGroupMembersRequestDTO(Long groupId, List<Long> memberIds) {
}

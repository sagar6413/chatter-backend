package com.chatapp.backend.dto.request;

import lombok.Builder;
import java.util.Set;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;

@Builder
public record CreateGroupRequest (
    @NotEmpty(message = "Participant IDs are required")
    @Size(min = 1, max = 256, message = "Number of participants must be between 1 and 256")
    Set<Long> participantIds,
    GroupSettingsRequest groupSettings
) {    
}

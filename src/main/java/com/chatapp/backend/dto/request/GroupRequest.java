package com.chatapp.backend.dto.request;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;
import lombok.Builder;

import java.util.Set;

@Builder
public record GroupRequest(@NotEmpty(message = "Participant IDs are required") @Size(min = 1, max = 256, message = "Number of participants must be between 1 and 256") Set<String> participantUsernames,
                           GroupSettingsRequest groupSettings) {
}

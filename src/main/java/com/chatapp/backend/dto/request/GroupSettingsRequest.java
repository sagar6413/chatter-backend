package com.chatapp.backend.dto.request;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Builder;

@Builder
public record GroupSettingsRequest(@NotBlank(message = "Group name is required") @Size(min = 3, max = 100, message = "Group name must be between 3 and 100 characters") String name,

                                   @Size(max = 500, message = "Description cannot exceed 500 characters") String description,

                                   boolean onlyAdminsCanSend,

                                   @Min(value = 1, message = "Message retention days must be at least 1") @Max(value = 365, message = "Message retention days cannot exceed 365") Integer messageRetentionDays,

                                   @Min(value = 2, message = "Maximum members must be at least 2") @Max(value = 256, message = "Maximum members cannot exceed 256") Integer maxMembers,

                                   boolean isPublic) {
}

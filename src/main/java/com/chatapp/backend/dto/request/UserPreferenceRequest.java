package com.chatapp.backend.dto.request;

import jakarta.validation.constraints.Pattern;
import lombok.Builder;

@Builder
public record UserPreferenceRequest(
     boolean notificationEnabled,

    @Pattern(regexp = "^(light|dark)$", message = "Theme must be either 'light' or 'dark'")
     String theme){
}

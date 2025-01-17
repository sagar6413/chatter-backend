package com.chatapp.backend.dto.response;

import com.chatapp.backend.entity.enums.Theme;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Builder;

@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public record UserPreferenceResponse(boolean notificationEnabled, Theme theme) {
}
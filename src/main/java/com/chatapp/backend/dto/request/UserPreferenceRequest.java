package com.chatapp.backend.dto.request;

import com.chatapp.backend.entity.enums.Theme;
import lombok.Builder;

@Builder
public record UserPreferenceRequest(boolean notificationEnabled, Theme theme) {
}

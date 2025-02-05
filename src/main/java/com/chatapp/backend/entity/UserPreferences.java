package com.chatapp.backend.entity;

import com.chatapp.backend.entity.enums.Theme;
import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.*;
import lombok.experimental.SuperBuilder;

@Embeddable
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
public class UserPreferences {
    @Column(name = "notification_enabled")
    @Builder.Default
    private boolean notificationEnabled = true;

    @Column(name = "theme")
    @Builder.Default
    private Theme theme = Theme.DARK;

    // Add more user preferences as needed
}


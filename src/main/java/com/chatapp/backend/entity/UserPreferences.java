package com.chatapp.backend.entity;

import com.chatapp.backend.entity.enums.Theme;
import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

@Embeddable
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
public class UserPreferences  {
    @Column(name = "notification_enabled")
    private boolean notificationEnabled = true;

    @Column(name = "theme")
    private Theme theme = Theme.DARK;

    // Add more user preferences as needed
}


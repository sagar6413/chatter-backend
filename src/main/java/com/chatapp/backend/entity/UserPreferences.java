package com.chatapp.backend.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;

@Embeddable
public class UserPreferences {
    @Column(name = "notification_enabled")
    private boolean notificationEnabled = true;

    @Column(name = "theme")
    private String theme = "light";

    // Add more user preferences as needed
    
}

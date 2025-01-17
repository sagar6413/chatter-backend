package com.chatapp.backend.dto.response;

import com.chatapp.backend.entity.User;

import java.util.Set;

public record GroupSettingsResponse(String name,
                                    String description,
                                    User creator,
                                    boolean onlyAdminsCanSend,
                                    Integer messageRetentionDays,
                                    Integer maxMembers,
                                    boolean isPublic,
                                    Set<User> admins) {
}

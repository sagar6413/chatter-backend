package com.chatapp.backend.entity;

import jakarta.persistence.*;
import lombok.*;

import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "group_settings")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class GroupSettings extends BaseEntity {
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "conversation_id", nullable = false)
    private Conversation conversation;

    @Column(name = "name", nullable = false, length = 100)
    private String name;

    @Column(length = 500)
    private String description;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "creator_id", nullable = false)
    private User creator;

    @Builder.Default
    @Column(name = "only_admins_can_send")
    private boolean onlyAdminsCanSend = false;

    @Column(name = "message_retention_days")
    private Integer messageRetentionDays;

    @Column(name = "max_members")
    private Integer maxMembers = 256;

    @Column(name = "is_public")
    private boolean isPublic = false;

    @OneToMany(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private Set<User> admins = new HashSet<>();
}

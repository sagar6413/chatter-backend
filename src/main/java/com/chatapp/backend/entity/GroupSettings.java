package com.chatapp.backend.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.UpdateTimestamp;
import org.springframework.data.annotation.CreatedDate;

import java.time.Instant;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "group_settings")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class GroupSettings {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    @CreatedDate
    @Column(name = "created_at", updatable = false, nullable = false)
    private Instant createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    @Version
    @Column(nullable = false)
    private Long version;

    @Column(name = "is_deleted", nullable = false)
    @Builder.Default
    private boolean deleted = false;
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
    @Builder.Default
    private Integer maxMembers = 256;
    @Column(name = "is_public")
    @Builder.Default
    private boolean isPublic = false;
    // Use ManyToMany with a join table
    @ManyToMany
    @JoinTable(
            name = "group_admins",
            joinColumns = @JoinColumn(name = "group_settings_id"),
            inverseJoinColumns = @JoinColumn(name = "user_id"),
            indexes = {
                    @Index(name = "idx_group_admin_user", columnList = "user_id"),
                    @Index(name = "idx_group_admin_settings", columnList = "group_settings_id")
            }
    )
    @Builder.Default
    private Set<User> admins = new HashSet<>();

    @PreUpdate
    public void preUpdate() {
        if (deleted && updatedAt == null) {
            this.updatedAt = Instant.now();
        }
    }
}
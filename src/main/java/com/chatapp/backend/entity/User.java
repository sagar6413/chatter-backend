package com.chatapp.backend.entity;

import com.chatapp.backend.entity.enums.UserStatus;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.BatchSize;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.Instant;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "users",
        indexes = {
                @Index(name = "idx_user_username", columnList = "username", unique = true),
                @Index(name = "idx_user_last_active", columnList = "last_active_at")
        }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class User extends BaseEntity {
    @Column(name = "username", unique = true, nullable = false, length = 50)
    private String username;

    @Column(name = "display_name", nullable = false, length = 100)
    private String displayName;

    @Column(name = "password", nullable = false)
    private String password; // Consider using a more secure field for password storage

    @Enumerated(EnumType.STRING)
    @Column(name = "status", length = 20, nullable = false)
    @Builder.Default
    private UserStatus status = UserStatus.OFFLINE;

    @Column(name = "last_seen_at")
    private Instant lastSeenAt;

    @Column(name = "unread_messages_count")
    private int unreadMessagesCount;

    @OneToMany(mappedBy = "sender")
    @BatchSize(size = 20)
    @Builder.Default
    private Set<Message> sentMessages = new HashSet<>();

    @ManyToMany(mappedBy = "participants")
    @BatchSize(size = 20)
    @Builder.Default
    private Set<Conversation> conversations = new HashSet<>();

    @Embedded
    @AttributeOverrides({
            @AttributeOverride(name = "theme",
                    column = @Column(name = "ui_theme")),
            @AttributeOverride(name = "notificationEnabled",
                    column = @Column(name = "notifications_enabled"))
    })
    private UserPreferences preferences = new UserPreferences();

}


package com.chatapp.backend.entity;

import com.chatapp.backend.entity.enums.UserStatus;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;
import org.hibernate.annotations.BatchSize;
import org.hibernate.annotations.UpdateTimestamp;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.security.Principal;
import java.time.Instant;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Entity
@Table(name = "users", indexes = {@Index(name = "idx_user_username", columnList = "username", unique = true)})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class User implements UserDetails, Principal {
    @Column(name = "refresh_token", length = 500)
    String refreshToken;
    @Column(name = "email_verified")
    boolean emailVerified;
    @Column(name = "is_active")
    boolean isActive;
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)  // Recommended for PostgreSQL
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
    @Column(name = "username", unique = true, nullable = false, length = 50)
    private String username;
    @Column(name = "email", unique = true, nullable = false, length = 100)
    private String email;
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
    @OneToMany(mappedBy = "sender")
    @BatchSize(size = 20)
    @Builder.Default
    private Set<Message> sentMessages = new HashSet<>();
    @ManyToMany(mappedBy = "participants")
    @BatchSize(size = 20)
    @Builder.Default
    private Set<Conversation> conversations = new HashSet<>();
    @Embedded
    @AttributeOverrides({@AttributeOverride(name = "theme", column = @Column(name = "ui_theme")), @AttributeOverride(name = "notificationEnabled", column = @Column(name = "notifications_enabled"))})
    @Builder.Default
    private UserPreferences preferences = new UserPreferences();

    @PreUpdate
    public void preUpdate() {
        if (deleted && updatedAt == null) {
            this.updatedAt = Instant.now();
        }
    }

    /**
     * Returns the authorities granted to the user. Cannot return <code>null</code>.
     *
     * @return the authorities, sorted by natural key (never <code>null</code>)
     */
    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return List.of();
    }

    @Override
    public String getPassword() {
        return password;
    }

    @Override
    public String getUsername() {
        return username;
    }

    @Override
    public boolean isAccountNonExpired() {
        return isActive;
    }

    @Override
    public boolean isAccountNonLocked() {
        return emailVerified;
    }

    @Override
    public boolean isEnabled() {
        return isActive;
    }

    @Override
    public String getName() {
        return username;
    }
}


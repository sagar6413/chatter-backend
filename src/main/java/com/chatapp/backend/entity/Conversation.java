package com.chatapp.backend.entity;

import com.chatapp.backend.entity.enums.ConversationType;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;
import org.hibernate.annotations.BatchSize;
import org.hibernate.annotations.UpdateTimestamp;
import org.springframework.data.annotation.CreatedDate;

import java.time.Instant;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "conversations",
        indexes = {
                @Index(name = "idx_conversation_type", columnList = "type"),
                @Index(name = "idx_conversation_created", columnList = "created_at")
        }
)
@Getter
@Setter
@SuperBuilder
@AllArgsConstructor
@NoArgsConstructor
public class Conversation {
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
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private ConversationType type;
    // Optimize fetching of participants
    @ManyToMany
    @JoinTable(
            name = "conversation_participants",
            joinColumns = @JoinColumn(name = "conversation_id"),
            inverseJoinColumns = @JoinColumn(name = "user_id"),
            indexes = {
                    @Index(name = "idx_conv_part_user", columnList = "user_id"),
                    @Index(name = "idx_conv_part_conv", columnList = "conversation_id")
            }
    )
    @BatchSize(size = 30)
    @Builder.Default
    private Set<User> participants = new HashSet<>();
    // Add participant count for quick access
    @Column(name = "participant_count")
    private int participantCount;
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "last_message_id")
    private Message lastMessage;
    // Use @OneToOne with lazy loading and cascade
    @OneToOne(mappedBy = "conversation",
            cascade = CascadeType.ALL
    )
    private GroupSettings groupSettings;


    // Add helper methods for managing participants
    public void addParticipant(User user) {
        participants.add(user);
        user.getConversations().add(this);
    }

    public void removeParticipant(User user) {
        participants.remove(user);
        user.getConversations().remove(this);
    }

    public void updateParticipantCount() {
        this.participantCount = this.participants.size();
    }

    @PrePersist
    @PreUpdate
    private void validateParticipants() {
        if (type == ConversationType.PRIVATE && participants.size() > 2) {
            throw new IllegalStateException("Direct conversations can only have 2 participants");
        }
    }
}



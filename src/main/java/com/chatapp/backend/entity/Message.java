package com.chatapp.backend.entity;

import com.chatapp.backend.entity.enums.MessageDeliveryState;
import com.chatapp.backend.entity.enums.MessageStatus;
import com.chatapp.backend.entity.enums.MessageType;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.BatchSize;
import org.hibernate.annotations.CreationTimestamp;

import java.time.Instant;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

@Entity
@Table(name = "messages",
        indexes = {
                @Index(name = "idx_message_conversation", columnList = "conversation_id, created_at"),
                @Index(name = "idx_message_sender", columnList = "sender_id"),
                @Index(name = "idx_message_status", columnList = "status")
        }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Message extends BaseEntity{
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "conversation_id", nullable = false)
    private Conversation conversation;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "sender_id", nullable = false)
    private User sender;

    @Column(length = 4000)
    private String content;

    @Column(name = "edited_at")
    private Instant editedAt;

    @OneToMany(mappedBy = "message", cascade = CascadeType.ALL, orphanRemoval = true)
    @BatchSize(size = 20)
    @Builder.Default
    private Set<Media> mediaItems = new HashSet<>();

    @Enumerated(EnumType.STRING)
    @Column(name = "message_type", nullable = false, length = 20)
    private MessageType type;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private MessageStatus status;

    @OneToMany(mappedBy = "message", cascade = CascadeType.ALL)
    @BatchSize(size = 50)
    @Builder.Default
    private Set<MessageDeliveryStatus> deliveryStatuses = new HashSet<>();

    @OneToMany(mappedBy = "message", cascade = CascadeType.ALL)
    @BatchSize(size = 30)
    @Builder.Default
    private Set<Reaction> reactions = new HashSet<>();

    @PrePersist
    protected void onCreate() {
        if (status == null)
            status = MessageStatus.NOT_SENT;

        if (type == null)
            type = MessageType.TEXT;

    }

    // Add helper methods for managing relationships
    public void addMediaItem(Media mediaItem) {
        mediaItems.add(mediaItem);
        mediaItem.setMessage(this);
    }
    public void removeMediaItem(Media mediaItem) {
        mediaItems.remove(mediaItem);
        mediaItem.setMessage(null);
    }
    public void addReaction(Reaction reaction) {
        reactions.add(reaction);
        reaction.setMessage(this);
    }
    public void removeReaction(Reaction reaction) {
        reactions.remove(reaction);
        reaction.setMessage(null);
    }
    public void editContent(String newContent) {
        this.content = newContent;
        this.editedAt = Instant.now();
    }
    public void updateMessageStatus(MessageStatus newStatus) {
        this.status = newStatus;
    }

    public void initializeDeliveryStatus(Set<User> recipients) {
        recipients.forEach(recipient -> {
            if (!this.sender.equals(recipient)) {
                MessageDeliveryStatus status = MessageDeliveryStatus.builder()
                                                                    .message(this)
                                                                    .recipient(recipient)
                                                                    .status(MessageDeliveryState.SENT)
                                                                    .sentAt(Instant.now())
                                                                    .build();
                this.deliveryStatuses.add(status);
            }
        });
    }

    // Get count of recipients who have read the message
    public long getReadCount() {
        return deliveryStatuses.stream()
                               .filter(status -> status.getStatus() == MessageDeliveryState.READ)
                               .count();
    }

    // Get recipients who haven't read the message yet
    public Set<User> getUnreadRecipients() {
        return deliveryStatuses.stream()
                               .filter(status -> status.getStatus() != MessageDeliveryState.READ)
                               .map(MessageDeliveryStatus::getRecipient)
                               .collect(Collectors.toSet());
    }

    // Check if all recipients have read the message
    public boolean isReadByAll() {
        return deliveryStatuses.stream()
                               .allMatch(status -> status.getStatus() == MessageDeliveryState.READ);
    }
}
package com.chatapp.backend.entity;

import com.chatapp.backend.entity.enums.MessageStatus;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.UpdateTimestamp;
import org.springframework.data.annotation.CreatedDate;

import java.time.Instant;

@Entity
@Table(name = "message_delivery_status",
        uniqueConstraints = {
                @UniqueConstraint(
                        columnNames = {"message_id", "recipient_id"},
                        name = "uk_message_recipient"
                )
        },
        indexes = {
                @Index(name = "idx_mds_message_status", columnList = "message_id,status"),
                @Index(name = "idx_mds_recipient_status", columnList = "recipient_id,status")
        }
)
@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class MessageDeliveryStatus {
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
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "message_id", nullable = false)
    private Message message;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "recipient_id", nullable = false)
    private User recipient;
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private MessageStatus status;
    @Column(name = "status_timestamp", nullable = false)
    private Instant statusTimestamp;

    @PreUpdate
    public void preUpdate() {
        if (deleted && updatedAt == null) {
            this.updatedAt = Instant.now();
        }
    }

    // Add helper methods for state transitions
    public void markAsReceived() {
        if (this.status.ordinal() < MessageStatus.RECEIVED.ordinal()) {
            this.status = MessageStatus.RECEIVED;
            this.statusTimestamp = Instant.now();
        }
    }

    public void markAsDelivered() {
        if (this.status.ordinal() < MessageStatus.DELIVERED.ordinal()) {
            this.status = MessageStatus.DELIVERED;
            this.statusTimestamp = Instant.now();
        }
    }

    public void markAsRead() {
        if (this.status.ordinal() < MessageStatus.READ.ordinal()) {
            this.status = MessageStatus.READ;
            this.statusTimestamp = Instant.now();
        }
    }
}


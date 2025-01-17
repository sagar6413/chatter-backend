package com.chatapp.backend.entity;

import com.chatapp.backend.entity.enums.MediaType;
import com.chatapp.backend.entity.enums.MediaUploadStatus;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.Instant;

@Entity
@Table(name = "media",
        indexes = {
                @Index(name = "idx_media_message", columnList = "message_id"),
                @Index(name = "idx_media_type", columnList = "file_type")
        }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Media extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "message_id", nullable = false)
    private Message message;

    @Column(name = "file_name", nullable = false)
    private String fileName;

    @Column(name = "file_type", nullable = false, length = 50)
    @Enumerated(EnumType.STRING)
    private MediaType fileType;


    @Column(name = "file_size", nullable = false)
    private Long fileSize;

    @Column(name = "upload_url", nullable = false)
    private String uploadUrl;

    @CreationTimestamp
    @Column(name = "uploaded_at", updatable = false)
    private Instant uploadedAt;
    // Store metadata separately for quick access
    @Column(name = "metadata", columnDefinition = "jsonb")
    private String metadata;

    // Add status tracking for upload progress
    @Enumerated(EnumType.STRING)
    @Column(name = "upload_status")
    private MediaUploadStatus uploadStatus = MediaUploadStatus.PENDING;

    @Column(name = "checksum")
    private String checksum;
}


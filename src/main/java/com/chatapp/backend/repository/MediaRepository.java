package com.chatapp.backend.repository;

import com.chatapp.backend.entity.Media;
import com.chatapp.backend.entity.enums.MediaUploadStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;

@Repository
public interface MediaRepository extends JpaRepository<Media, Long> {
    List<Media> findByMessageId(Long messageId);

    List<Media> findByUploadStatus(MediaUploadStatus uploadStatus);

    Page<Media> findAll(Pageable pageable);

    @Query("SELECT m FROM Media m WHERE m.uploadStatus = ?1 AND m.createdAt < ?2")
    List<Media> findStaleUploads(MediaUploadStatus status, Instant before);

}

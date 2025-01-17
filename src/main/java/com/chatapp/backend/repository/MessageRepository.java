package com.chatapp.backend.repository;

import com.chatapp.backend.entity.Message;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

@Repository
public interface MessageRepository extends JpaRepository<Message, Long> {

    // Find messages in a conversation with efficient loading of related entities
    @Query("SELECT DISTINCT m FROM Message m " +
            "LEFT JOIN FETCH m.sender s " +
            "LEFT JOIN FETCH m.mediaItems " +
            "LEFT JOIN FETCH m.reactions " +
            "WHERE m.conversation.id = :conversationId " +
            "ORDER BY m.createdAt DESC")
    Page<Message> findByConversationId(
            @Param("conversationId") Long conversationId,
            Pageable pageable);

    // Get the latest message for a conversation with sender details
    @Query("SELECT m FROM Message m " +
            "LEFT JOIN FETCH m.sender " +
            "WHERE m.conversation.id = :conversationId " +
            "ORDER BY m.createdAt DESC " +
            "LIMIT 1")
    Optional<Message> findLatestMessageByConversationId(
            @Param("conversationId") Long conversationId);

    // Find messages by a specific user in a conversation
    @Query("SELECT m FROM Message m " +
            "LEFT JOIN FETCH m.mediaItems " +
            "LEFT JOIN FETCH m.reactions " +
            "WHERE m.conversation.id = :conversationId " +
            "AND m.sender.username = :username " +
            "ORDER BY m.createdAt DESC")
    Page<Message> findByConversationIdAndSenderUsername(
            @Param("conversationId") Long conversationId,
            @Param("username") String username,
            Pageable pageable);

    // Find messages containing specific text (useful for search functionality)
    @Query("SELECT m FROM Message m " +
            "LEFT JOIN FETCH m.sender " +
            "WHERE m.conversation.id = :conversationId " +
            "AND LOWER(m.content) LIKE LOWER(CONCAT('%', :searchText, '%')) " +
            "ORDER BY m.createdAt DESC")
    Page<Message> searchMessagesInConversation(
            @Param("conversationId") Long conversationId,
            @Param("searchText") String searchText,
            Pageable pageable);

    // Find messages within a specific date range
    @Query("SELECT m FROM Message m " +
            "LEFT JOIN FETCH m.sender " +
            "WHERE m.conversation.id = :conversationId " +
            "AND m.createdAt BETWEEN :startDate AND :endDate " +
            "ORDER BY m.createdAt DESC")
    Page<Message> findMessagesByDateRange(
            @Param("conversationId") Long conversationId,
            @Param("startDate") Instant startDate,
            @Param("endDate") Instant endDate,
            Pageable pageable);

    // Count messages in a conversation
    @Query("SELECT COUNT(m) FROM Message m " +
            "WHERE m.conversation.id = :conversationId")
    long countMessagesByConversationId(
            @Param("conversationId") Long conversationId);

    // Find messages with media attachments
    @Query("SELECT DISTINCT m FROM Message m " +
            "JOIN FETCH m.mediaItems mi " +
            "LEFT JOIN FETCH m.sender " +
            "WHERE m.conversation.id = :conversationId " +
            "AND m.type = 'MEDIA' " +
            "ORDER BY m.createdAt DESC")
    Page<Message> findMessagesWithMedia(
            @Param("conversationId") Long conversationId,
            Pageable pageable);

    // Delete messages older than retention period
    @Modifying
    @Query("DELETE FROM Message m " +
            "WHERE m.conversation.id = :conversationId " +
            "AND m.createdAt < :retentionDate")
    int deleteMessagesOlderThanRetention(
            @Param("conversationId") Long conversationId,
            @Param("retentionDate") Instant retentionDate);

    Page<Message> findByConversationIdOrderByCreatedAtDesc(Long conversationId, Pageable pageable);

    @Query("SELECT m FROM Message m LEFT JOIN FETCH m.mediaItems " +
            "WHERE m.conversation.id = ?1 AND m.createdAt < ?2 ORDER BY m.createdAt DESC")
    List<Message> findMessageHistory(Long conversationId, Instant before, Pageable pageable);

    @Query("SELECT COUNT(m) FROM Message m WHERE m.conversation.id = ?1 AND m.createdAt > ?2")
    long countUnreadMessages(Long conversationId, Instant since);
}
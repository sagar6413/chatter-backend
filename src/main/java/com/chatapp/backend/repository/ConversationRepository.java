package com.chatapp.backend.repository;

import com.chatapp.backend.entity.Conversation;
import com.chatapp.backend.entity.Message;
import com.chatapp.backend.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.Optional;
import java.util.Set;

@Repository
public interface ConversationRepository extends JpaRepository<Conversation, Long> {

    // Find a private conversation between two specific users
    @Query("SELECT c FROM Conversation c " +
            "JOIN c.participants p1 " +
            "JOIN c.participants p2 " +
            "WHERE c.type = 'PRIVATE' " +
            "AND p1.username = :username1 " +
            "AND p2.username = :username2")
    Optional<Conversation> findPrivateConversationBetweenUsers(
            @Param("username1") String username1,
            @Param("username2") String username2);

    // Find all private conversations for a user with latest message
    @Query("SELECT DISTINCT c FROM Conversation c " +
            "LEFT JOIN FETCH c.lastMessage " +
            "JOIN c.participants p " +
            "WHERE c.type = 'PRIVATE' " +
            "AND p.username = :username " +
            "ORDER BY c.lastMessage.createdAt DESC")
    Page<Conversation> findPrivateConversationsByUserUsername(
            @Param("username") String username,
            Pageable pageable);

    // Find all group conversations for a user with group settings
    @Query("SELECT DISTINCT c FROM Conversation c " +
            "LEFT JOIN FETCH c.groupSettings gs " +
            "LEFT JOIN FETCH c.lastMessage " +
            "JOIN c.participants p " +
            "WHERE c.type = 'GROUP' " +
            "AND p.username = :username " +
            "ORDER BY c.lastMessage.createdAt DESC")
    Page<Conversation> findGroupConversationsByParticipantUsername(
            @Param("username") String username,
            Pageable pageable);

    // Find all conversations (both private and group) for a user
    @Query("SELECT DISTINCT c FROM Conversation c " +
            "LEFT JOIN FETCH c.lastMessage " +
            "LEFT JOIN FETCH c.groupSettings gs " +
            "JOIN c.participants p " +
            "WHERE p.username = :username " +
            "ORDER BY c.lastMessage.createdAt DESC")
    Page<Conversation> findConversationsByParticipantUsername(
            @Param("username") String username,
            Pageable pageable);

    // Find public group conversations
    @Query("SELECT DISTINCT c FROM Conversation c " +
            "LEFT JOIN FETCH c.groupSettings gs " +
            "WHERE c.type = 'GROUP' " +
            "AND gs.isPublic = true " +
            "ORDER BY c.participantCount DESC")
    Page<Conversation> findPublicGroupConversations(Pageable pageable);

    // Find conversations by participant count range
    @Query("SELECT c FROM Conversation c " +
            "WHERE c.type = 'GROUP' " +
            "AND c.participantCount BETWEEN :minCount AND :maxCount " +
            "ORDER BY c.participantCount DESC")
    Page<Conversation> findGroupConversationsByParticipantCountRange(
            @Param("minCount") int minCount,
            @Param("maxCount") int maxCount,
            Pageable pageable);

    // Count active conversations for a user
    @Query("SELECT COUNT(c) FROM Conversation c " +
            "JOIN c.participants p " +
            "WHERE p.username = :username " +
            "AND c.lastMessage.createdAt > :since")
    long countActiveConversations(
            @Param("username") String username,
            @Param("since") Instant since);


    @Modifying
    @Query("UPDATE Conversation c SET c.lastMessage = ?2 WHERE c.id = ?1")
    void updateLastMessage(Long conversationId, Message message);
}
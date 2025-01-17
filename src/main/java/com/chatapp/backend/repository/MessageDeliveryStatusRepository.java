package com.chatapp.backend.repository;

import com.chatapp.backend.entity.MessageDeliveryStatus;
import com.chatapp.backend.entity.enums.MessageStatus;
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
public interface MessageDeliveryStatusRepository extends JpaRepository<MessageDeliveryStatus, Long> {

    // Find the delivery status for a specific message and recipient
    Optional<MessageDeliveryStatus> findByMessageIdAndRecipientUsername(Long messageId, String username);

    // Get all unread messages for a user with their associated conversations
    @Query("SELECT DISTINCT mds FROM MessageDeliveryStatus mds " +
            "JOIN FETCH mds.message m " +
            "JOIN FETCH m.conversation c " +
            "WHERE mds.recipient.username = :username " +
            "AND mds.status = 'UNREAD' " +
            "ORDER BY m.createdAt DESC")
    Page<MessageDeliveryStatus> findUnreadMessageStatusesForUser(
            @Param("username") String username,
            Pageable pageable);



    // Count unread messages per conversation for a user
    @Query("SELECT m.conversation.id as conversationId, COUNT(mds) as unreadCount " +
            "FROM MessageDeliveryStatus mds " +
            "JOIN mds.message m " +
            "WHERE mds.recipient.username = :username " +
            "AND mds.status = 'UNREAD' " +
            "GROUP BY m.conversation.id")
    List<UnreadMessageCount> countUnreadMessagesPerConversation(@Param("username") String username);

    // Find latest delivery status for each recipient in a message
    @Query("SELECT mds FROM MessageDeliveryStatus mds " +
            "WHERE mds.message.id = :messageId " +
            "ORDER BY mds.createdAt DESC")
    List<MessageDeliveryStatus> findDeliveryStatusesByMessageId(@Param("messageId") Long messageId);

    // Delete old delivery statuses for messages before a certain date
    @Modifying
    @Query("DELETE FROM MessageDeliveryStatus mds " +
            "WHERE mds.message.createdAt < :beforeDate")
    int deleteOldDeliveryStatuses(@Param("beforeDate") Instant beforeDate);
}

// Interface to handle the unread count projection
interface UnreadMessageCount {
    Long getConversationId();
    Long getUnreadCount();
}
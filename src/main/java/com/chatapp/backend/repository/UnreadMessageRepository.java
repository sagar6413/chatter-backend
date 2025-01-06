package com.chatapp.backend.repository;

import com.chatapp.backend.entity.UnreadMessage;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface UnreadMessageRepository extends JpaRepository<UnreadMessage, Long> {

    List<UnreadMessage> findUnreadMessagesForUserAndChat(Long userId, Long chatId, Long lastReadMessageId);

}

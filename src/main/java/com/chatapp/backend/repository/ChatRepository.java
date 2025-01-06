package com.chatapp.backend.repository;

import com.chatapp.backend.entity.Chat;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ChatRepository extends JpaRepository<Chat, Long> {

    Optional<Chat> findOneToOneChatByParticipants(Long id, Long id2);

    List<Chat> findChatsWithUnreadMessages(Long userId);

    List<Chat> findChatsByUserId(Long userId);
}

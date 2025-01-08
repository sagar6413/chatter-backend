package com.chatapp.backend.repository;

import com.chatapp.backend.entity.Conversation;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ContactRepository extends JpaRepository<Conversation, Long> {

    Optional<Conversation> findOneToOneContactByParticipants(Long id, Long id2);

    List<Conversation> findContactsWithUnreadMessages(Long userId);

    List<Conversation> findContactsByUserId(Long userId);
}

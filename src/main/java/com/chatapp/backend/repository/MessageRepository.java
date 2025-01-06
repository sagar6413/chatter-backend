package com.chatapp.backend.repository;

import aj.org.objectweb.asm.commons.Remapper;
import com.chatapp.backend.entity.Message;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface MessageRepository extends JpaRepository<Message, Long> {

    List<Message> findByChatId(Long chatId);

    Message findLatestMessage(Long id);
}

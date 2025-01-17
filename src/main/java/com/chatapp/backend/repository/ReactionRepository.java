package com.chatapp.backend.repository;

import com.chatapp.backend.entity.Reaction;
import com.chatapp.backend.entity.enums.ReactionType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ReactionRepository extends JpaRepository<Reaction, Long> {
    Optional<Reaction> findByMessage_IdAndUser_Username(Long messageId, String username);
    List<Reaction> findByMessageId(Long messageId);
    List<Reaction> findByType(ReactionType type);

    Page<Reaction> findAll(Pageable pageable);

    @Query("SELECT r.type, COUNT(r) FROM Reaction r WHERE r.message.id = ?1 GROUP BY r.type")
    List<Object[]> countReactionsByType(Long messageId);
}

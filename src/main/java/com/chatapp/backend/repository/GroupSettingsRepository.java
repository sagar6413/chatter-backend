package com.chatapp.backend.repository;

import com.chatapp.backend.entity.GroupSettings;
import com.chatapp.backend.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface GroupSettingsRepository extends JpaRepository<GroupSettings, Long> {
    Optional<GroupSettings> findByConversationId(Long conversationId);

    List<GroupSettings> findByCreatorUsername(String username);


    List<GroupSettings> findByIsPublicTrue();

    Page<GroupSettings> findAll(Pageable pageable);

    @Query("SELECT CASE WHEN COUNT(g) > 0 THEN true ELSE false END FROM GroupSettings g " +
            "WHERE g.conversation.id = ?1 AND ?2 MEMBER OF g.admins")
    boolean isUserAdmin(Long conversationId, User user);
}

package com.chatapp.backend.repository;

import com.chatapp.backend.entity.GroupParticipant;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface GroupMembershipRepository extends JpaRepository<GroupParticipant, Long> {
    List<GroupParticipant> findByGroupIdAndIsActiveTrue(Long groupId);

    Optional<GroupParticipant> findByUserIdAndGroupId(Long userId, Long groupId);
}

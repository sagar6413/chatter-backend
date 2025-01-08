package com.chatapp.backend.repository;

import com.chatapp.backend.entity.GroupSetting;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface GroupMembershipRepository extends JpaRepository<GroupSetting, Long> {
    List<GroupSetting> findByGroupIdAndIsActiveTrue(Long groupId);

    Optional<GroupSetting> findByUserIdAndGroupId(Long userId, Long groupId);
}

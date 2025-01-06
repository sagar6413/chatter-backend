package com.chatapp.backend.repository;

import com.chatapp.backend.entity.Group;
import org.springframework.data.jpa.repository.JpaRepository;

public interface GroupRepository extends JpaRepository<Group, Long> {
}

package com.chatapp.backend.repository;

import com.chatapp.backend.entity.Media;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MediaRepository extends JpaRepository<Media, Long> {
}

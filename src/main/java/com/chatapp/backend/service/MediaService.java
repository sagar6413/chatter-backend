package com.chatapp.backend.service;

import com.chatapp.backend.entity.Media;
import org.springframework.web.multipart.MultipartFile;

public interface MediaService {
    Media uploadMedia(MultipartFile file, Long messageId);
}

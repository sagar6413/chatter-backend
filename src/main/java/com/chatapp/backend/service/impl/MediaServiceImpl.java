package com.chatapp.backend.service.impl;

import com.chatapp.backend.entity.Media;
import com.chatapp.backend.service.MediaService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

@Service
@RequiredArgsConstructor
public class MediaServiceImpl implements MediaService {
    @Override
    public Media uploadMedia(MultipartFile file, Long messageId) {
        return null;
    }
}

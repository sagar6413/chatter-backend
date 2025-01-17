package com.chatapp.backend.service;

import com.chatapp.backend.dto.request.MediaUploadRequest;
import com.chatapp.backend.entity.Media;
import jakarta.validation.Valid;
import org.springframework.web.multipart.MultipartFile;

public interface MediaService {
    Media uploadMedia(MultipartFile file, Long messageId);

    Object initializeUpload(@Valid MediaUploadRequest request);

    Object uploadFile(Long mediaId, MultipartFile file);

    Object getMedia(Long mediaId);

    Object deleteMedia(Long mediaId);
}

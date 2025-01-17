package com.chatapp.backend.service.impl;

import com.chatapp.backend.dto.request.MediaUploadRequest;
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

    @Override
    public Object initializeUpload(MediaUploadRequest request) {
        return null;
    }

    @Override
    public Object uploadFile(Long mediaId, MultipartFile file) {
        return null;
    }

    @Override
    public Object getMedia(Long mediaId) {
        return null;
    }

    @Override
    public Object deleteMedia(Long mediaId) {
        return null;
    }
}

package com.chatapp.backend.controller;

import com.chatapp.backend.entity.Media;
import com.chatapp.backend.service.MediaService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

@RestController
@RequestMapping("/api/media")
@RequiredArgsConstructor
public class MediaController {

    private final MediaService mediaService;

    @PostMapping(value = "/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<Media> uploadMedia(@RequestParam("file") MultipartFile file,
                                             @RequestParam("messageId") Long messageId) throws IOException {
        Media uploadedMedia = mediaService.uploadMedia(file, messageId);
        return new ResponseEntity<>(uploadedMedia, HttpStatus.CREATED);
    }
}
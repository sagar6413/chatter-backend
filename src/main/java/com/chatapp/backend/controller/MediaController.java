package com.chatapp.backend.controller;

import com.chatapp.backend.dto.request.MediaUploadRequest;
import com.chatapp.backend.entity.Media;
import com.chatapp.backend.service.MediaService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

/**
 * Controller for handling media-related operations such as uploading,
 * retrieving, and deleting media files.
 */
@RestController
@RequestMapping("/api/media")
@RequiredArgsConstructor
public class MediaController {

    private static final Logger LOGGER = LoggerFactory.getLogger(MediaController.class);
    private final MediaService mediaService;

    /**
     * Upload a media file and associate it with a message.
     *
     * @param file      the media file to upload
     * @param messageId the ID of the message the media is associated with
     * @return ResponseEntity containing the uploaded Media object
     * @throws IOException if an error occurs during file upload
     */
    @PostMapping(value = "/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<Media> uploadMedia(@RequestParam("file") MultipartFile file, @RequestParam("messageId") Long messageId) throws IOException {
        LOGGER.info("Received request to upload media for messageId: {}", messageId);
        return new ResponseEntity<>(mediaService.uploadMedia(file, messageId), HttpStatus.CREATED);
    }

    /**
     * Initialize the upload process for a media file.
     *
     * @param request the request containing initialization details
     * @return ResponseEntity containing initialization result
     */
    @PostMapping("/initialize")
    public ResponseEntity<?> initializeUpload(@Valid @RequestBody MediaUploadRequest request) {
        LOGGER.info("Initializing media upload with request: {}", request);
        return ResponseEntity.ok(mediaService.initializeUpload(request));
    }

    /**
     * Upload a file to an existing media entity.
     *
     * @param mediaId the ID of the media to which the file will be uploaded
     * @param file    the file to upload
     * @return ResponseEntity containing the upload result
     */
    @PostMapping(value = "/upload/{mediaId}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<?> uploadFile(@PathVariable Long mediaId, @RequestParam("file") MultipartFile file) {
        LOGGER.info("Uploading file to mediaId: {}", mediaId);
        return ResponseEntity.ok(mediaService.uploadFile(mediaId, file));
    }

    /**
     * Retrieve media details by its ID.
     *
     * @param mediaId the ID of the media to retrieve
     * @return ResponseEntity containing the media details
     */
    @GetMapping("/{mediaId}")
    public ResponseEntity<?> getMedia(@PathVariable Long mediaId) {
        LOGGER.info("Fetching media details for mediaId: {}", mediaId);
        return ResponseEntity.ok(mediaService.getMedia(mediaId));
    }

    /**
     * Delete a media entity by its ID.
     *
     * @param mediaId the ID of the media to delete
     * @return ResponseEntity indicating the result of the delete operation
     */
    @DeleteMapping("/{mediaId}")
    public ResponseEntity<?> deleteMedia(@PathVariable Long mediaId) {
        LOGGER.info("Deleting media with mediaId: {}", mediaId);
        return ResponseEntity.ok(mediaService.deleteMedia(mediaId));
    }
}

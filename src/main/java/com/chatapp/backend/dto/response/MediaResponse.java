package com.chatapp.backend.dto.response;

import com.chatapp.backend.entity.enums.MediaUploadStatus;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Builder;

@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public record MediaResponse(Long id,
                            String fileName,
                            String fileType,
                            Long fileSize,
                            String uploadUrl,
                            MediaUploadStatus uploadStatus,
                            String metadata) {
}

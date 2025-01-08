package com.chatapp.backend.dto.request;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.Builder;

@Builder
public record MediaUploadRequest(
    @NotBlank(message = "File name is required")
    String fileName,

    @NotNull(message = "File size is required")
    @Min(value = 1, message = "File size must be greater than 0")
    @Max(value = 104857600, message = "File size cannot exceed 100MB") // 100MB limit
    Long fileSize,

    @NotBlank(message = "File type is required")
    String fileType,

    @Pattern(regexp = "^[a-fA-F0-9]{32}$", message = "Invalid checksum format")
    String checksum
) {
    
}

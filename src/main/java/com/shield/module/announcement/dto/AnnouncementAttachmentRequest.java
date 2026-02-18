package com.shield.module.announcement.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record AnnouncementAttachmentRequest(
        @NotBlank @Size(max = 255) String fileName,
        @NotBlank @Size(max = 1000) String fileUrl,
        Long fileSize,
        @Size(max = 100) String contentType) {
}

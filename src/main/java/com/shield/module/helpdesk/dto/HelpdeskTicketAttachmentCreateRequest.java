package com.shield.module.helpdesk.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record HelpdeskTicketAttachmentCreateRequest(
        @NotBlank @Size(max = 255) String fileName,
        @NotBlank @Size(max = 2000) String fileUrl
) {
}

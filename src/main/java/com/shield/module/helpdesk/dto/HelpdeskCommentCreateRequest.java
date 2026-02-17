package com.shield.module.helpdesk.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record HelpdeskCommentCreateRequest(
        @NotBlank @Size(max = 2000) String comment,
        boolean internalNote
) {
}

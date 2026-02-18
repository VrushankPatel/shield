package com.shield.module.complaint.dto;

import jakarta.validation.constraints.NotBlank;

public record ComplaintCommentUpdateRequest(@NotBlank String comment) {
}

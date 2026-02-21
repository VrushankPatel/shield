package com.shield.module.complaint.dto;

import jakarta.validation.constraints.NotBlank;

public record ComplaintCommentCreateRequest(@NotBlank String comment) {
}

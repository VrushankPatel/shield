package com.shield.module.move.dto;

import jakarta.validation.constraints.Size;

public record MoveRecordDecisionRequest(
        @Size(max = 500) String decisionNotes
) {
}

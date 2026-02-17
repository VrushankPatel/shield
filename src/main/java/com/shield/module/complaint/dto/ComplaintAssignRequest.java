package com.shield.module.complaint.dto;

import jakarta.validation.constraints.NotNull;
import java.util.UUID;

public record ComplaintAssignRequest(@NotNull UUID assignedTo) {
}

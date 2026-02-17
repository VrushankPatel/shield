package com.shield.module.helpdesk.dto;

import com.shield.module.helpdesk.entity.TicketPriority;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.util.UUID;

public record HelpdeskTicketUpdateRequest(
        UUID categoryId,
        UUID unitId,
        @NotBlank @Size(max = 255) String subject,
        @Size(max = 20000) String description,
        @NotNull TicketPriority priority
) {
}

package com.shield.module.helpdesk.dto;

import jakarta.validation.constraints.NotNull;
import java.util.UUID;

public record HelpdeskTicketAssignRequest(@NotNull UUID assignedTo) {
}

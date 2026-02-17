package com.shield.module.helpdesk.dto;

import jakarta.validation.constraints.Size;

public record HelpdeskTicketResolveRequest(@Size(max = 2000) String resolutionNotes) {
}

package com.shield.module.helpdesk.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;

public record HelpdeskTicketRateRequest(
        @Min(1) @Max(5) int satisfactionRating
) {
}

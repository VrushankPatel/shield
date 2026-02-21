package com.shield.module.helpdesk.dto;

import java.math.BigDecimal;

public record HelpdeskTicketStatsResponse(
        long totalTickets,
        long openTickets,
        long inProgressTickets,
        long resolvedTickets,
        long closedTickets,
        long overdueTickets,
        BigDecimal averageSatisfactionRating
) {
}

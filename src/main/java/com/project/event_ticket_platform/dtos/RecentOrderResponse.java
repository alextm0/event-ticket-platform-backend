package com.project.event_ticket_platform.dtos;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public record RecentOrderResponse(
        UUID id,
        String userName,
        String ticketSummary, // e.g. "VIP x2" or just "VIP"
        BigDecimal amount,
        Instant timestamp) {
}

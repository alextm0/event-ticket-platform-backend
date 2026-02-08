package com.project.event_ticket_platform.dtos;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public record RecentOrderResponse(
        UUID id,
        @JsonProperty("user") String userName,
        @JsonProperty("ticket") String ticketSummary,
        BigDecimal amount,
        Instant timestamp) {
}

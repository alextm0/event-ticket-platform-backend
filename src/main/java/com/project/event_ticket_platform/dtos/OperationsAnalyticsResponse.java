package com.project.event_ticket_platform.dtos;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Operations analytics for an event")
public record OperationsAnalyticsResponse(
        @Schema(description = "Number of tickets checked in") long checkedInCount,
        @Schema(description = "Total tickets sold") long totalSold,
        @Schema(description = "No-show ratio from 0.0 to 1.0 (1.0 - checkedIn/totalSold), same convention as soldRatio") double noShowRate) {
}

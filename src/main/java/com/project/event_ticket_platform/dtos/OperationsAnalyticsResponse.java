package com.project.event_ticket_platform.dtos;

public record OperationsAnalyticsResponse(
        long checkedInCount,
        long totalSold,
        double noShowRate) {
}

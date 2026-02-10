package com.project.event_ticket_platform.controllers;

import com.project.event_ticket_platform.dtos.OperationsAnalyticsResponse;
import com.project.event_ticket_platform.dtos.RecentOrderResponse;
import com.project.event_ticket_platform.dtos.SalesHistoryPoint;
import com.project.event_ticket_platform.services.AnalyticsService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/events/{eventId}")
@Tag(name = "Analytics", description = "Endpoints for event analytics and monitoring")
@RequiredArgsConstructor
public class AnalyticsController {

    private final AnalyticsService analyticsService;

    @Operation(summary = "Get sales history", description = "Get revenue and sales count over time")
    @GetMapping("/analytics/sales-history")
    @PreAuthorize("@eventAuthorizationService.isOrganizer(#eventId, authentication.principal)")
    public ResponseEntity<List<SalesHistoryPoint>> getSalesHistory(@PathVariable UUID eventId) {
        return ResponseEntity.ok(analyticsService.getSalesHistory(eventId));
    }

    @Operation(summary = "Get recent orders", description = "Get a paginated list of recent orders for the event")
    @GetMapping("/orders")
    @PreAuthorize("@eventAuthorizationService.isOrganizer(#eventId, authentication.principal)")
    public ResponseEntity<Page<RecentOrderResponse>> getRecentOrders(
            @PathVariable UUID eventId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        Pageable pageable = PageRequest.of(page, size);
        return ResponseEntity.ok(analyticsService.getRecentOrders(eventId, pageable));
    }

    @Operation(summary = "Get operations analytics", description = "Get live operations statistics like check-ins and no-show rate")
    @GetMapping("/analytics/operations")
    @PreAuthorize("@eventAuthorizationService.isOrganizer(#eventId, authentication.principal)")
    public ResponseEntity<OperationsAnalyticsResponse> getOperationsAnalytics(@PathVariable UUID eventId) {
        return ResponseEntity.ok(analyticsService.getOperationsAnalytics(eventId));
    }
}

package com.project.event_ticket_platform.controllers;

import com.project.event_ticket_platform.dtos.OperationsAnalyticsResponse;
import com.project.event_ticket_platform.dtos.RecentOrderResponse;
import com.project.event_ticket_platform.dtos.SalesHistoryPoint;
import com.project.event_ticket_platform.entities.Event;
import com.project.event_ticket_platform.entities.User;
import com.project.event_ticket_platform.entities.UserRole;
import com.project.event_ticket_platform.exceptions.EventNotFoundException;
import com.project.event_ticket_platform.exceptions.UnauthorizedAccessException;
import com.project.event_ticket_platform.exceptions.UserNotFoundException;
import com.project.event_ticket_platform.repositories.EventRepository;
import com.project.event_ticket_platform.repositories.UserRepository;
import com.project.event_ticket_platform.services.AnalyticsService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/events/{eventId}")
@Tag(name = "Analytics", description = "Endpoints for event analytics and monitoring")
@RequiredArgsConstructor
public class AnalyticsController {

    private final AnalyticsService analyticsService;
    private final EventRepository eventRepository;
    private final UserRepository userRepository;

    @Operation(summary = "Get sales history", description = "Get revenue and sales count over time")
    @GetMapping("/analytics/sales-history")
    public ResponseEntity<List<SalesHistoryPoint>> getSalesHistory(
            @PathVariable UUID eventId,
            @RequestHeader("X-User-Id") UUID userId) {
        validateOrganizerAccess(eventId, userId);
        return ResponseEntity.ok(analyticsService.getSalesHistory(eventId));
    }

    @Operation(summary = "Get recent orders", description = "Get a paginated list of recent orders for the event")
    @GetMapping("/orders")
    public ResponseEntity<Page<RecentOrderResponse>> getRecentOrders(
            @PathVariable UUID eventId,
            @RequestHeader("X-User-Id") UUID userId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        validateOrganizerAccess(eventId, userId);
        Pageable pageable = PageRequest.of(page, size);
        return ResponseEntity.ok(analyticsService.getRecentOrders(eventId, pageable));
    }

    @Operation(summary = "Get operations analytics", description = "Get live operations statistics like check-ins and no-show rate")
    @GetMapping("/analytics/operations")
    public ResponseEntity<OperationsAnalyticsResponse> getOperationsAnalytics(
            @PathVariable UUID eventId,
            @RequestHeader("X-User-Id") UUID userId) {
        validateOrganizerAccess(eventId, userId);
        return ResponseEntity.ok(analyticsService.getOperationsAnalytics(eventId));
    }

    private void validateOrganizerAccess(UUID eventId, UUID userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException(userId));

        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new EventNotFoundException(eventId));

        // Only the organizer (owner) or Admin can view analytics
        // Staff typically don't see revenue, but maybe operations?
        // For now restricting to Organizer/Admin
        boolean isOrganizer = event.getOrganizer().getId().equals(userId);
        boolean isAdmin = user.getRole() == UserRole.ADMIN;

        if (!isOrganizer && !isAdmin) {
            throw new UnauthorizedAccessException("Only the event organizer can view analytics.");
        }
    }
}

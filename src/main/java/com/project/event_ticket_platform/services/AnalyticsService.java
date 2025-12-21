package com.project.event_ticket_platform.services;

import com.project.event_ticket_platform.dtos.OperationsAnalyticsResponse;
import com.project.event_ticket_platform.dtos.RecentOrderResponse;
import com.project.event_ticket_platform.dtos.SalesHistoryPoint;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.UUID;

public interface AnalyticsService {

    List<SalesHistoryPoint> getSalesHistory(UUID eventId);

    Page<RecentOrderResponse> getRecentOrders(UUID eventId, Pageable pageable);

    OperationsAnalyticsResponse getOperationsAnalytics(UUID eventId);
}

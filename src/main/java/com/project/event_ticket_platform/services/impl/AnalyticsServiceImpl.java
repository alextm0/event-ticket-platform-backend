package com.project.event_ticket_platform.services.impl;

import com.project.event_ticket_platform.dtos.OperationsAnalyticsResponse;
import com.project.event_ticket_platform.dtos.RecentOrderResponse;
import com.project.event_ticket_platform.dtos.SalesHistoryPoint;
import com.project.event_ticket_platform.entities.TicketOrder;
import com.project.event_ticket_platform.repositories.TicketOrderRepository;
import com.project.event_ticket_platform.repositories.TicketRepository;
import com.project.event_ticket_platform.services.AnalyticsService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AnalyticsServiceImpl implements AnalyticsService {

    private final TicketRepository ticketRepository;
    private final TicketOrderRepository ticketOrderRepository;

    @Override
    public List<SalesHistoryPoint> getSalesHistory(UUID eventId) {
        List<Object[]> results = ticketRepository.getSalesHistory(eventId);
        return results.stream()
                .map(row -> new SalesHistoryPoint(
                        row[0].toString(), // Date
                        (BigDecimal) row[1], // Revenue
                        (Long) row[2] // Sales count
                ))
                .collect(Collectors.toList());
    }

    @Override
    public Page<RecentOrderResponse> getRecentOrders(UUID eventId, Pageable pageable) {
        Page<TicketOrder> orders = ticketOrderRepository.findByEventId(eventId, pageable);

        return orders.map(order -> {
            // Summarize tickets: "VIP x2, General x1"
            // Filter tickets for this event only (in case of mixed orders, though unlikely
            // in current flow)
            Map<String, Long> ticketCounts = order.getTickets().stream()
                    .filter(t -> t.getTicketType().getEvent().getId().equals(eventId))
                    .collect(Collectors.groupingBy(
                            t -> t.getTicketType().getName(),
                            Collectors.counting()));

            String summary = ticketCounts.entrySet().stream()
                    .map(e -> e.getKey() + " (" + e.getValue() + ")")
                    .collect(Collectors.joining(", "));

            // Calculate total amount for this event's tickets only
            BigDecimal eventOrderTotal = order.getTickets().stream()
                    .filter(t -> t.getTicketType().getEvent().getId().equals(eventId))
                    .map(t -> t.getTicketType().getPrice())
                    .reduce(BigDecimal.ZERO, BigDecimal::add);

            return new RecentOrderResponse(
                    order.getId(),
                    order.getBuyerName(),
                    summary,
                    eventOrderTotal,
                    order.getCreatedAt());
        });
    }

    @Override
    public OperationsAnalyticsResponse getOperationsAnalytics(UUID eventId) {
        long totalSold = ticketRepository.countByEventId(eventId);
        long checkedIn = ticketRepository.countCheckedInByEventId(eventId);

        double noShowRate = 0.0;
        if (totalSold > 0) {
            noShowRate = (1.0 - ((double) checkedIn / totalSold)) * 100.0;
        }

        return new OperationsAnalyticsResponse(checkedIn, totalSold, noShowRate);
    }
}

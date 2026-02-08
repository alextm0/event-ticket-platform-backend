package com.project.event_ticket_platform.services.impl;

import com.project.event_ticket_platform.dtos.OperationsAnalyticsResponse;
import com.project.event_ticket_platform.dtos.RecentOrderResponse;
import com.project.event_ticket_platform.dtos.SalesHistoryPoint;
import com.project.event_ticket_platform.entities.Event;
import com.project.event_ticket_platform.entities.Ticket;
import com.project.event_ticket_platform.entities.TicketOrder;
import com.project.event_ticket_platform.entities.TicketType;
import com.project.event_ticket_platform.repositories.TicketOrderRepository;
import com.project.event_ticket_platform.repositories.TicketRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.within;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AnalyticsServiceImplTest {

    @Mock
    private TicketRepository ticketRepository;

    @Mock
    private TicketOrderRepository ticketOrderRepository;

    @InjectMocks
    private AnalyticsServiceImpl analyticsService;

    private UUID eventId;

    @BeforeEach
    void setUp() {
        eventId = UUID.randomUUID();
    }

    @Test
    void getSalesHistory_eventWithSales_returnsHistoryPoints() {
        java.time.LocalDate date = java.time.LocalDate.of(2024, 1, 15);
        BigDecimal revenue = new BigDecimal("250.00");
        Long sales = 5L;
        List<Object[]> results = Collections.singletonList(new Object[]{date, revenue, sales});

        when(ticketRepository.getSalesHistory(eventId)).thenReturn(results);

        List<SalesHistoryPoint> actual = analyticsService.getSalesHistory(eventId);

        assertThat(actual).hasSize(1);
        assertThat(actual.get(0).date()).isEqualTo("2024-01-15");
        assertThat(actual.get(0).revenue()).isEqualByComparingTo("250.00");
        assertThat(actual.get(0).sales()).isEqualTo(5L);
    }

    @Test
    void getSalesHistory_eventWithNoSales_returnsEmptyList() {
        when(ticketRepository.getSalesHistory(eventId)).thenReturn(List.of());

        List<SalesHistoryPoint> actual = analyticsService.getSalesHistory(eventId);

        assertThat(actual).isEmpty();
    }

    @Test
    void getRecentOrders_eventWithOrders_returnsPage() {
        Event event = new Event();
        event.setId(eventId);
        TicketType ticketType = new TicketType();
        ticketType.setEvent(event);
        ticketType.setName("VIP");
        ticketType.setPrice(new BigDecimal("50.00"));

        Ticket ticket = new Ticket();
        ticket.setTicketType(ticketType);

        TicketOrder order = new TicketOrder();
        order.setId(UUID.randomUUID());
        order.setBuyerName("John Doe");
        order.setCreatedAt(Instant.now());
        order.setTickets(List.of(ticket));
        ticket.setOrder(order);

        Page<TicketOrder> orderPage = new PageImpl<>(List.of(order), PageRequest.of(0, 10), 1);
        when(ticketOrderRepository.findByEventId(eq(eventId), any(Pageable.class))).thenReturn(orderPage);

        Page<RecentOrderResponse> actual = analyticsService.getRecentOrders(eventId, PageRequest.of(0, 10));

        assertThat(actual.getContent()).hasSize(1);
        assertThat(actual.getContent().get(0).userName()).isEqualTo("John Doe");
        assertThat(actual.getContent().get(0).ticketSummary()).contains("VIP");
        assertThat(actual.getContent().get(0).amount()).isEqualByComparingTo("50.00");
    }

    @Test
    void getRecentOrders_eventWithNoOrders_returnsEmptyPage() {
        Page<TicketOrder> emptyPage = new PageImpl<>(List.of(), PageRequest.of(0, 10), 0);
        when(ticketOrderRepository.findByEventId(eq(eventId), any(Pageable.class))).thenReturn(emptyPage);

        Page<RecentOrderResponse> actual = analyticsService.getRecentOrders(eventId, PageRequest.of(0, 10));

        assertThat(actual.getContent()).isEmpty();
        assertThat(actual.getTotalElements()).isZero();
    }

    @Test
    void getOperationsAnalytics_eventWithTickets_returnsCorrectCounts() {
        when(ticketRepository.countByEventId(eventId)).thenReturn(100L);
        when(ticketRepository.countCheckedInByEventId(eventId)).thenReturn(85L);

        OperationsAnalyticsResponse actual = analyticsService.getOperationsAnalytics(eventId);

        assertThat(actual.totalSold()).isEqualTo(100L);
        assertThat(actual.checkedInCount()).isEqualTo(85L);
        assertThat(actual.noShowRate()).isCloseTo(15.0, within(0.001));
    }

    @Test
    void getOperationsAnalytics_eventWithNoTickets_returnsZeroValues() {
        when(ticketRepository.countByEventId(eventId)).thenReturn(0L);
        when(ticketRepository.countCheckedInByEventId(eventId)).thenReturn(0L);

        OperationsAnalyticsResponse actual = analyticsService.getOperationsAnalytics(eventId);

        assertThat(actual.totalSold()).isZero();
        assertThat(actual.checkedInCount()).isZero();
        assertThat(actual.noShowRate()).isZero();
    }

    @Test
    void getOperationsAnalytics_partialCheckIns_calculatesNoShowRateAsPercentage() {
        when(ticketRepository.countByEventId(eventId)).thenReturn(10L);
        when(ticketRepository.countCheckedInByEventId(eventId)).thenReturn(7L);

        OperationsAnalyticsResponse actual = analyticsService.getOperationsAnalytics(eventId);

        assertThat(actual.noShowRate()).isCloseTo(30.0, within(0.001));
    }
}

package com.project.event_ticket_platform.controllers;

import com.project.event_ticket_platform.dtos.OperationsAnalyticsResponse;
import com.project.event_ticket_platform.dtos.RecentOrderResponse;
import com.project.event_ticket_platform.dtos.SalesHistoryPoint;
import com.project.event_ticket_platform.exceptions.EventNotFoundException;
import com.project.event_ticket_platform.services.AnalyticsService;
import com.project.event_ticket_platform.services.EventAuthorizationService;
import com.project.event_ticket_platform.services.JwtService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.project.event_ticket_platform.config.SecurityConfig;
import com.project.event_ticket_platform.config.TestMethodSecurityConfig;
import org.springframework.context.annotation.Import;

@WebMvcTest(controllers = AnalyticsController.class, excludeAutoConfiguration = {
        org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration.class
}, excludeFilters = {
        @ComponentScan.Filter(type = FilterType.ASSIGNABLE_TYPE, classes = SecurityConfig.class)
})
@Import(TestMethodSecurityConfig.class)
@AutoConfigureMockMvc(addFilters = false)
@TestPropertySource(properties = {"app.jpa.auditing.enabled=false"})
class AnalyticsControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private AnalyticsService analyticsService;

    @MockitoBean(name = "eventAuthorizationService")
    private EventAuthorizationService eventAuthorizationService;

    @MockitoBean
    private JwtService jwtService;

    private UUID organizerId;

    @BeforeEach
    void setUp() {
        organizerId = UUID.randomUUID();
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(organizerId, null, List.of()));
        when(eventAuthorizationService.isOrganizer(any(UUID.class), eq(organizerId))).thenReturn(true);
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void getSalesHistory_organizer_returns200() throws Exception {
        UUID eventId = UUID.randomUUID();

        when(analyticsService.getSalesHistory(eventId)).thenReturn(List.of(
                new SalesHistoryPoint("2024-01-15", new BigDecimal("250.00"), 5L)
        ));

        mockMvc.perform(get("/api/v1/events/{eventId}/analytics/sales-history", eventId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].date").value("2024-01-15"))
                .andExpect(jsonPath("$[0].revenue").value(250.0))
                .andExpect(jsonPath("$[0].sales").value(5));

        verify(analyticsService).getSalesHistory(eventId);
    }

    @Test
    void getSalesHistory_eventNotFound_returns404() throws Exception {
        UUID eventId = UUID.randomUUID();

        when(analyticsService.getSalesHistory(eventId))
                .thenThrow(new EventNotFoundException(eventId));

        mockMvc.perform(get("/api/v1/events/{eventId}/analytics/sales-history", eventId))
                .andExpect(status().isNotFound());
    }

    @Test
    void getSalesHistory_nonOrganizer_returns403() throws Exception {
        UUID eventId = UUID.randomUUID();
        UUID nonOrganizerId = UUID.randomUUID();
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(nonOrganizerId, null, List.of()));
        when(eventAuthorizationService.isOrganizer(eq(eventId), eq(nonOrganizerId))).thenReturn(false);

        mockMvc.perform(get("/api/v1/events/{eventId}/analytics/sales-history", eventId))
                .andExpect(status().isForbidden());
    }

    @Test
    void getRecentOrders_organizer_returns200() throws Exception {
        UUID eventId = UUID.randomUUID();
        UUID orderId = UUID.randomUUID();

        RecentOrderResponse order = new RecentOrderResponse(
                orderId, "John Doe", "VIP (2)", new BigDecimal("100.00"), Instant.now());
        Page<RecentOrderResponse> page = new PageImpl<>(List.of(order), PageRequest.of(0, 10), 1);

        when(analyticsService.getRecentOrders(eq(eventId), any())).thenReturn(page);

        mockMvc.perform(get("/api/v1/events/{eventId}/orders", eventId)
                        .param("page", "0")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].user").value("John Doe"))
                .andExpect(jsonPath("$.content[0].ticket").value("VIP (2)"))
                .andExpect(jsonPath("$.content[0].amount").value(100.0));

        verify(analyticsService).getRecentOrders(eq(eventId), any());
    }

    @Test
    void getOperationsAnalytics_organizer_returns200() throws Exception {
        UUID eventId = UUID.randomUUID();

        when(analyticsService.getOperationsAnalytics(eventId))
                .thenReturn(new OperationsAnalyticsResponse(85, 100, 15.0));

        mockMvc.perform(get("/api/v1/events/{eventId}/analytics/operations", eventId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.checkedInCount").value(85))
                .andExpect(jsonPath("$.totalSold").value(100))
                .andExpect(jsonPath("$.noShowRate").value(15.0));

        verify(analyticsService).getOperationsAnalytics(eventId);
    }

    @Test
    void getOperationsAnalytics_nonOrganizer_returns403() throws Exception {
        UUID eventId = UUID.randomUUID();
        UUID nonOrganizerId = UUID.randomUUID();
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(nonOrganizerId, null, List.of()));
        when(eventAuthorizationService.isOrganizer(eq(eventId), eq(nonOrganizerId))).thenReturn(false);

        mockMvc.perform(get("/api/v1/events/{eventId}/analytics/operations", eventId))
                .andExpect(status().isForbidden());
    }
}

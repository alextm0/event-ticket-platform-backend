package com.project.event_ticket_platform.dtos;

import com.project.event_ticket_platform.entities.OrderStatus;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

public record PurchaseTicketResponse(
	UUID orderId,
	BigDecimal totalAmount,
	OrderStatus orderStatus,
	List<TicketResponse> tickets,
	Instant purchasedAt
) {
}


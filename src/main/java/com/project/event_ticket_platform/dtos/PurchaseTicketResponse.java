package com.project.event_ticket_platform.dtos;

import com.project.event_ticket_platform.entities.OrderStatus;
import io.swagger.v3.oas.annotations.media.Schema;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

@Schema(description = "Ticket purchase response")
public record PurchaseTicketResponse(
	@Schema(description = "Order ID", example = "123e4567-e89b-12d3-a456-426614174000")
	UUID orderId,

	@Schema(description = "Total amount", example = "300.00")
	BigDecimal totalAmount,

	@Schema(description = "Order status")
	OrderStatus orderStatus,

	@Schema(description = "Tickets purchased")
	List<TicketResponse> tickets
) {
}


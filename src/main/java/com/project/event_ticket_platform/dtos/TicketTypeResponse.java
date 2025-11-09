package com.project.event_ticket_platform.dtos;

import io.swagger.v3.oas.annotations.media.Schema;

import java.math.BigDecimal;
import java.util.UUID;

@Schema(description = "Ticket type information")
public record TicketTypeResponse(
	@Schema(description = "Ticket type ID", example = "123e4567-e89b-12d3-a456-426614174000")
	UUID id,

	@Schema(description = "Ticket type name", example = "VIP")
	String name,

	@Schema(description = "Ticket type description", example = "VIP access with backstage pass")
	String description,

	@Schema(description = "Ticket price", example = "150.00")
	BigDecimal price,

	@Schema(description = "Total quantity", example = "100")
	Integer totalQuantity,

	@Schema(description = "Sold count", example = "45")
	Integer soldCount,

	@Schema(description = "Available quantity", example = "55")
	Integer availableQuantity,

	@Schema(description = "Is active", example = "true")
	boolean active
) {
}


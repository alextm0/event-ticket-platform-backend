package com.project.event_ticket_platform.dtos;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

@Schema(description = "Request to purchase ticket")
public record PurchaseTicketRequest(
	@Schema(description = "Quantity to purchase", example = "2")
	@NotNull(message = "Quantity is required")
	@Min(value = 1, message = "Quantity must be at least 1")
	Integer quantity
) {
}


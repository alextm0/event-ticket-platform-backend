package com.project.event_ticket_platform.dtos;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

public record CreateTicketTypeRequest(
	@NotBlank String name,
	String description,
	@NotNull @DecimalMin(value = "0.0", inclusive = true) BigDecimal price,
	@NotNull @Min(0) Integer totalQuantity,
	Boolean active
) {
	public boolean isActive() {
		return active == null || active;
	}
}


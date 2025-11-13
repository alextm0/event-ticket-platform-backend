package com.project.event_ticket_platform.dtos;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;

public record CreateTicketTypeRequest(
	@NotBlank @Size(max = 255) String name,
	@Size(max = 5000) String description,
	@NotNull @DecimalMin(value = "0.0", inclusive = true) BigDecimal price,
	@NotNull @Min(1) Integer totalQuantity,
	Boolean active
) {
	public boolean isActive() {
		return active == null || active;
	}
}


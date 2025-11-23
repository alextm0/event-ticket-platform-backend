package com.project.event_ticket_platform.dtos;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;

@Schema(description = "Payload to validate a ticket by providing its QR code data")
public record ValidateTicketRequest(
	@Schema(
		description = "QR code data string from the scanned ticket (format: TICKET:{uuid}|EVENT:{uuid}|USER:{uuid}|ORDER:{uuid}|TIMESTAMP:{timestamp})",
		example = "TICKET:123e4567-e89b-12d3-a456-426614174000|EVENT:123e4567-e89b-12d3-a456-426614174001|USER:123e4567-e89b-12d3-a456-426614174002|ORDER:123e4567-e89b-12d3-a456-426614174003|TIMESTAMP:1234567890",
		required = true
	)
	@NotNull(message = "QR code data is required")
	String qrCodeId
) {
}

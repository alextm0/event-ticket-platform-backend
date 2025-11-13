package com.project.event_ticket_platform.dtos;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;

import java.util.UUID;

@Schema(description = "Payload to validate a ticket by providing its QR code identifier")
public record ValidateTicketRequest(
	@Schema(
		description = "QR code identifier embedded in the attendee's ticket",
		example = "123e4567-e89b-12d3-a456-426614174000",
		required = true
	)
	@NotNull(message = "QR code id is required")
	UUID qrCodeId
) {
}

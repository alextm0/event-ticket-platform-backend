package com.project.event_ticket_platform.dtos;

import com.project.event_ticket_platform.entities.TicketStatus;
import com.project.event_ticket_platform.entities.TicketValidationEnum;
import com.project.event_ticket_platform.entities.TicketValidationMethodEnum;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.Instant;
import java.util.UUID;

@Schema(description = "Details about a ticket validation attempt")
public record TicketValidationResponse(
	@Schema(description = "Validation identifier", example = "123e4567-e89b-12d3-a456-426614174001")
	UUID id,

	@Schema(description = "Event identifier the ticket belongs to", example = "123e4567-e89b-12d3-a456-426614174002")
	UUID eventId,

	@Schema(description = "Event title to quickly identify the venue", example = "Spring Gala 2025")
	String eventTitle,

	@Schema(description = "Ticket identifier that was validated", example = "123e4567-e89b-12d3-a456-426614174003")
	UUID ticketId,

	@Schema(description = "QR code identifier that was scanned", example = "123e4567-e89b-12d3-a456-426614174004")
	UUID qrCodeId,

	@Schema(description = "Current lifecycle state of the ticket")
	TicketStatus ticketStatus,

	@Schema(description = "Outcome of the validation attempt")
	TicketValidationEnum validationStatus,

	@Schema(description = "Mechanism that performed the validation")
	TicketValidationMethodEnum validationMethod,

	@Schema(description = "Timestamp when the validation was persisted in the system")
	Instant validatedAt
) {
}

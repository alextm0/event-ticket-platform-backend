package com.project.event_ticket_platform.dtos;

import com.project.event_ticket_platform.entities.TicketStatus;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.Instant;
import java.util.UUID;

@Schema(description = "Ticket information")
public record TicketResponse(
	@Schema(description = "Ticket ID", example = "123e4567-e89b-12d3-a456-426614174000")
	UUID id,

	@Schema(description = "Ticket status")
	TicketStatus status,

	@Schema(description = "Event title", example = "Spring Music Festival")
	String eventTitle,

	@Schema(description = "Event location", example = "Central Park")
	String eventLocation,

	@Schema(description = "Event start time")
	Instant eventStartTime,

	@Schema(description = "Ticket type name", example = "VIP")
	String ticketTypeName,

	@Schema(description = "QR code ID", example = "123e4567-e89b-12d3-a456-426614174000")
	UUID qrCodeId,

	@Schema(description = "Checked in at")
	Instant checkedInAt,

	@Schema(description = "Created at")
	Instant createdAt
) {
}


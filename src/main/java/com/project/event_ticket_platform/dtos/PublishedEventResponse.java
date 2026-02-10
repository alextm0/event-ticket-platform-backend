package com.project.event_ticket_platform.dtos;

import com.project.event_ticket_platform.entities.EventStatus;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Schema(description = "Published event information with ticket types")
public record PublishedEventResponse(
	@Schema(description = "Event ID", example = "123e4567-e89b-12d3-a456-426614174000")
	UUID id,

	@Schema(description = "Event title", example = "Spring Music Festival")
	String title,

	@Schema(description = "Event description")
	String description,

	@Schema(description = "Event location", example = "Central Park")
	String location,

	@Schema(description = "Event start time")
	Instant startTime,

	@Schema(description = "Event end time")
	Instant endTime,

	@Schema(description = "Event status")
	EventStatus status,

	@Schema(description = "Organizer user ID", example = "123e4567-e89b-12d3-a456-426614174000")
	UUID organizerId,

	@Schema(description = "Organizer name", example = "John Doe")
	String organizerName,

	@Schema(description = "Available ticket types")
	List<TicketTypeResponse> ticketTypes
) {
}


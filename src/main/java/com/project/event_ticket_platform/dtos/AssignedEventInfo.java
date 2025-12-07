package com.project.event_ticket_platform.dtos;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.UUID;

@Schema(description = "Assigned event information for staff member")
public record AssignedEventInfo(
	@Schema(description = "Event ID", example = "123e4567-e89b-12d3-a456-426614174000")
	UUID eventId,

	@Schema(description = "Event title/name", example = "Spring Music Festival")
	String eventName
) {
}


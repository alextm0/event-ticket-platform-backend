package com.project.event_ticket_platform.dtos;

import com.project.event_ticket_platform.entities.EventStatus;

import java.time.Instant;
import java.util.UUID;

public record EventResponse(
	UUID id,
	UUID organizerId,
	String title,
	String description,
	String location,
	Instant startTime,
	Instant endTime,
	EventStatus status,
	Instant createdAt,
	Instant updatedAt
) {
}

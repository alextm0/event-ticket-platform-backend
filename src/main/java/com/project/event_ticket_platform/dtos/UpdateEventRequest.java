package com.project.event_ticket_platform.dtos;

import com.project.event_ticket_platform.entities.EventStatus;
import jakarta.validation.constraints.FutureOrPresent;

import java.time.Instant;

public record UpdateEventRequest(
	String title,
	String description,
	String location,
	@FutureOrPresent Instant startTime,
	@FutureOrPresent Instant endTime,
	EventStatus status
) {
}

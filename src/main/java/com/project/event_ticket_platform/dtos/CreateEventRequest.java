package com.project.event_ticket_platform.dtos;

import com.project.event_ticket_platform.entities.EventStatus;
import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.Instant;
import java.util.UUID;

public record CreateEventRequest(
	@NotNull UUID organizerId,
	@NotBlank String title,
	@NotBlank String description,
	@NotBlank String location,
	@NotNull @FutureOrPresent Instant startTime,
	@NotNull @Future Instant endTime,
	EventStatus status
) {
}

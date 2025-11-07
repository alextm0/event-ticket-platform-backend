package com.project.event_ticket_platform.dtos;

import com.project.event_ticket_platform.entities.UserRole;

import java.time.Instant;
import java.util.UUID;

public record UserResponse(
	UUID id,
	String name,
	String email,
	UserRole role,
	Instant createdAt,
	Instant updatedAt
) {
}

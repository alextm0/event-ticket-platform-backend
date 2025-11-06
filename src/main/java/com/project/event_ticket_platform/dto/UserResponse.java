package com.project.event_ticket_platform.dto;

import com.project.event_ticket_platform.entity.UserRole;

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

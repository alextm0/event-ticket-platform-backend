package com.project.event_ticket_platform.dtos;

import com.project.event_ticket_platform.entities.UserRole;

import java.util.UUID;

public record LoginResponse(
	UUID userId,
	String email,
	UserRole role,
	String token
) {
}


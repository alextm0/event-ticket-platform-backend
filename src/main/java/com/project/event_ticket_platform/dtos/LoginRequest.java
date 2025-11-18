package com.project.event_ticket_platform.dtos;

import com.project.event_ticket_platform.entities.UserRole;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record LoginRequest(
	@Email(message = "Email must be a valid email address")
	@NotBlank(message = "Email is required")
	String email,

	@NotBlank(message = "Password is required")
	String password,

	@NotNull(message = "Role is required")
	UserRole role
) {
}


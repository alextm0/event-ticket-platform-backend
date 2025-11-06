package com.project.event_ticket_platform.dto;

import com.project.event_ticket_platform.entity.UserRole;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CreateUserRequest(
	@NotBlank String name,
	@Email @NotBlank String email,
	@NotBlank @Size(min = 8, message = "Password must be at least 8 characters long") String password,
	UserRole role
) {
}

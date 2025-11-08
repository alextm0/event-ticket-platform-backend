package com.project.event_ticket_platform.exceptions;

import java.util.UUID;

public class UserNotFoundException extends RuntimeException {

	public UserNotFoundException(UUID userId) {
		super("User not found with id: " + userId);
	}
}


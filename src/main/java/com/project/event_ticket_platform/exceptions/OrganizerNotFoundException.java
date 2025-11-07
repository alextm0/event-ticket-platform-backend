package com.project.event_ticket_platform.exceptions;

import java.util.UUID;

public class OrganizerNotFoundException extends RuntimeException {

	public OrganizerNotFoundException(UUID organizerId) {
		super("Organizer not found with id: " + organizerId);
	}
}

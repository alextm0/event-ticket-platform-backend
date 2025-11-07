package com.project.event_ticket_platform.exceptions;

import java.util.UUID;

public class EventNotFoundException extends RuntimeException {

	public EventNotFoundException(UUID eventId) {
		super("Event not found with id: " + eventId);
	}
}

package com.project.event_ticket_platform.exceptions;

import java.util.UUID;

public class EventNotPublishedException extends RuntimeException {

	public EventNotPublishedException(UUID eventId) {
		super("Event not published with id: " + eventId);
	}
}


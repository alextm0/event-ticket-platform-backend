package com.project.event_ticket_platform.exceptions;

import java.util.UUID;

public class InsufficientTicketsException extends RuntimeException {

	public InsufficientTicketsException(UUID ticketTypeId, int requested, int available) {
		super(String.format("Only %d tickets available for ticket type %s, but %d requested", available, ticketTypeId, requested));
	}
}


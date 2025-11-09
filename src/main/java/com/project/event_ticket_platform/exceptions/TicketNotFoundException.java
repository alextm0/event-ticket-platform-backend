package com.project.event_ticket_platform.exceptions;

import java.util.UUID;

public class TicketNotFoundException extends RuntimeException {

	public TicketNotFoundException(UUID ticketId) {
		super("Ticket not found with id: " + ticketId);
	}
}


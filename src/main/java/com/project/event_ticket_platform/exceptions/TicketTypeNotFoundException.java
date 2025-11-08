package com.project.event_ticket_platform.exceptions;

import java.util.UUID;

public class TicketTypeNotFoundException extends RuntimeException {

	public TicketTypeNotFoundException(UUID ticketTypeId) {
		super("Ticket type not found with id: " + ticketTypeId);
	}
}


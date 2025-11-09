package com.project.event_ticket_platform.exceptions;

import java.util.UUID;

public class TicketTypeNotActiveException extends RuntimeException {

	public TicketTypeNotActiveException(UUID ticketTypeId) {
		super("Ticket type " + ticketTypeId + " is not active");
	}
}


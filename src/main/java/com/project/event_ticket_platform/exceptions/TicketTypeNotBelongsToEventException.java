package com.project.event_ticket_platform.exceptions;

import java.util.UUID;

public class TicketTypeNotBelongsToEventException extends RuntimeException {

	public TicketTypeNotBelongsToEventException(UUID ticketTypeId, UUID eventId) {
		super("Ticket type " + ticketTypeId + " does not belong to event " + eventId);
	}
}


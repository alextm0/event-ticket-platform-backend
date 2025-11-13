package com.project.event_ticket_platform.exceptions;

import java.util.UUID;

public class TicketTypeInUseException extends RuntimeException {
    /**
     * Constructs an exception indicating the specified ticket type is currently in use and cannot be deleted.
     *
     * @param ticketTypeId the UUID of the ticket type that is in use
     */
    public TicketTypeInUseException(UUID ticketTypeId) {
        super("Ticket type with id " + ticketTypeId + " is in use and cannot be deleted.");
    }
}
package com.project.event_ticket_platform.exceptions;

import java.util.UUID;

public class TicketTypeInUseException extends RuntimeException {
    public TicketTypeInUseException(UUID ticketTypeId) {
        super("Ticket type with id " + ticketTypeId + " is in use and cannot be deleted.");
    }
}
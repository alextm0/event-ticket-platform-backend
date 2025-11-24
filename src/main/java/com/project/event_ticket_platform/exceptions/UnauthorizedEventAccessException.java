package com.project.event_ticket_platform.exceptions;

import java.util.UUID;

public class UnauthorizedEventAccessException extends RuntimeException {
    public UnauthorizedEventAccessException(UUID userId, UUID eventId) {
        super("User " + userId + " is not authorized to access or modify event " + eventId);
    }
}

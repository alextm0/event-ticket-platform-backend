package com.project.event_ticket_platform.services;

import com.project.event_ticket_platform.dtos.TicketValidationResponse;
import com.project.event_ticket_platform.dtos.ValidateTicketRequest;

import java.util.List;
import java.util.UUID;

public interface TicketValidationService {

	/**
	 * Validate a ticket via QR code and persist the attempt.
	 */
	TicketValidationResponse validateTicket(UUID eventId, UUID staffId, ValidateTicketRequest request);

	/**
	 * List all validation attempts for a given event.
	 */
	List<TicketValidationResponse> listValidations(UUID eventId, UUID staffId);
}

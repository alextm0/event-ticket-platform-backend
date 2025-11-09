package com.project.event_ticket_platform.services;

import com.project.event_ticket_platform.dtos.PurchaseTicketRequest;
import com.project.event_ticket_platform.dtos.PurchaseTicketResponse;
import com.project.event_ticket_platform.dtos.QrCodeResponse;
import com.project.event_ticket_platform.dtos.TicketResponse;

import java.util.List;
import java.util.UUID;

public interface TicketService {

	/**
	 * Purchase tickets for a published event
	 */
	PurchaseTicketResponse purchaseTicket(UUID eventId, UUID ticketTypeId, PurchaseTicketRequest request, UUID userId);

	/**
	 * List all tickets for a user
	 */
	List<TicketResponse> listUserTickets(UUID userId);

	/**
	 * Retrieve a specific ticket for a user
	 */
	TicketResponse getTicketById(UUID ticketId, UUID userId);

	/**
	 * Retrieve QR code for a ticket
	 */
	QrCodeResponse getTicketQrCode(UUID ticketId, UUID userId);
}

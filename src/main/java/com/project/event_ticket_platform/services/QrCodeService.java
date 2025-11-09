package com.project.event_ticket_platform.services;

import com.project.event_ticket_platform.entities.QrCode;
import com.project.event_ticket_platform.entities.Ticket;

public interface QrCodeService {

	/**
	 * Generate a QR code for a ticket
	 * Note: The QR code is created but not saved - it will be saved via cascade when the ticket is saved
	 */
	QrCode generateQrCode(Ticket ticket);
}

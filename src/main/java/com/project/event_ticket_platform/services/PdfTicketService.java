package com.project.event_ticket_platform.services;

import com.project.event_ticket_platform.dtos.TicketResponse;
import java.io.ByteArrayOutputStream;

public interface PdfTicketService {
	/**
	 * Generates a PDF ticket for download
	 * @param ticket The ticket response containing ticket details
	 * @param qrCodeImage The QR code image as byte array (PNG)
	 * @return ByteArrayOutputStream containing the PDF
	 */
	ByteArrayOutputStream generateTicketPdf(TicketResponse ticket, byte[] qrCodeImage);
}


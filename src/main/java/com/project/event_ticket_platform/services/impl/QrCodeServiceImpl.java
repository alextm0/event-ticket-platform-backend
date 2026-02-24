package com.project.event_ticket_platform.services.impl;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.WriterException;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;
import com.project.event_ticket_platform.entities.QrCode;
import com.project.event_ticket_platform.entities.QrCodeStatusEnum;
import com.project.event_ticket_platform.entities.Ticket;
import com.project.event_ticket_platform.exceptions.QrCodeGenerationException;
import com.project.event_ticket_platform.repositories.QrCodeRepository;
import com.project.event_ticket_platform.services.QrCodeService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.time.Instant;
import java.util.Base64;

@Service
public class QrCodeServiceImpl implements QrCodeService {

	private final QrCodeRepository qrCodeRepository;
	private static final int QR_CODE_WIDTH = 300;
	private static final int QR_CODE_HEIGHT = 300;

	public QrCodeServiceImpl(QrCodeRepository qrCodeRepository) {
		this.qrCodeRepository = qrCodeRepository;
	}

	@Override
	public QrCode generateQrCode(Ticket ticket) {
		try {
			// Create unique QR code data
			String qrData = generateQrData(ticket);

		// Generate QR code image
		byte[] qrCodeImage = generateQrCodeImage(qrData);
		String base64Image = Base64.getEncoder().encodeToString(qrCodeImage);

		QrCode qrCode = new QrCode();
		qrCode.setCodeData(base64Image);
		qrCode.setStatus(QrCodeStatusEnum.ACTIVE);
		return qrCode;

		} catch (WriterException | IOException e) {
			throw new QrCodeGenerationException("Failed to generate QR code for ticket: " + ticket.getId(), e);
		}
	}

	private String generateQrData(Ticket ticket) {
		String ticketId = ticket.getId() != null 
			? ticket.getId().toString() 
			: "TEMP-" + System.currentTimeMillis() + "-" + (int)(Math.random() * 10000);
			
		if (ticket.getTicketType() == null || ticket.getTicketType().getEvent() == null) {
			throw new IllegalStateException("Ticket must have ticket type with event before generating QR code");
		}
		if (ticket.getOrder() == null || ticket.getOrder().getUser() == null) {
			throw new IllegalStateException("Ticket must have order with user before generating QR code");
		}

		return String.format("TICKET:%s|EVENT:%s|USER:%s|ORDER:%s|TIMESTAMP:%s",
			ticketId,
			ticket.getTicketType().getEvent().getId(),
			ticket.getOrder().getUser().getId(),
			ticket.getOrder().getId() != null ? ticket.getOrder().getId().toString() : "TEMP",
			Instant.now().toEpochMilli()
		);
	}

	private byte[] generateQrCodeImage(String qrData) throws WriterException, IOException {
		QRCodeWriter qrCodeWriter = new QRCodeWriter();
		BitMatrix bitMatrix = qrCodeWriter.encode(
			qrData,
			BarcodeFormat.QR_CODE,
			QR_CODE_WIDTH,
			QR_CODE_HEIGHT
		);

		try (ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {
			MatrixToImageWriter.writeToStream(bitMatrix, "PNG", outputStream);
			return outputStream.toByteArray();
		}
	}
}


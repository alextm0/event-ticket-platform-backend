package com.project.event_ticket_platform.services;

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
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.time.Instant;
import java.util.Base64;
import java.util.UUID;

@Service
public class QrCodeService {

	private final QrCodeRepository qrCodeRepository;
	private static final int QR_CODE_WIDTH = 300;
	private static final int QR_CODE_HEIGHT = 300;

	public QrCodeService(QrCodeRepository qrCodeRepository) {
		this.qrCodeRepository = qrCodeRepository;
	}

	@Transactional
	public QrCode generateQrCode(Ticket ticket) {
		try {
			// Create unique QR code data
			String qrData = generateQrData(ticket);

			// Generate QR code image
			byte[] qrCodeImage = generateQrCodeImage(qrData);
			String base64Image = Base64.getEncoder().encodeToString(qrCodeImage);

			// Create and save QR code entity
			QrCode qrCode = new QrCode();
			qrCode.setCodeData(base64Image);
			qrCode.setStatus(QrCodeStatusEnum.ACTIVE);
			qrCode.setGeneratedDateTime(Instant.now());

			return qrCodeRepository.save(qrCode);

		} catch (WriterException | IOException e) {
			throw new QrCodeGenerationException("Failed to generate QR code for ticket: " + ticket.getId(), e);
		}
	}

	private String generateQrData(Ticket ticket) {
		// Create unique QR data containing ticket information
		return String.format("TICKET:%s|EVENT:%s|USER:%s|TIMESTAMP:%s",
			ticket.getId(),
			ticket.getTicketType().getEvent().getId(),
			ticket.getOrder().getUser().getId(),
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


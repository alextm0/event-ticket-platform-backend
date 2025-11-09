package com.project.event_ticket_platform.mappers;

import com.project.event_ticket_platform.dtos.QrCodeResponse;
import com.project.event_ticket_platform.entities.QrCode;
import org.springframework.stereotype.Component;

@Component
public class QrCodeMapper {

	public QrCodeResponse toResponse(QrCode qrCode) {
		// Generate QR code data - could be a URL or base64 encoded image
		// For now, we'll use a URL format: https://example.com/qr/{id}
		String qrCodeData = "https://api.event-platform.com/qr/" + qrCode.getId().toString();

		return new QrCodeResponse(
			qrCode.getId(),
			qrCodeData,
			qrCode.getStatus()
		);
	}
}


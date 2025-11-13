package com.project.event_ticket_platform.exceptions;

import java.util.UUID;

public class QrCodeNotFoundException extends RuntimeException {

	public QrCodeNotFoundException(UUID qrCodeId) {
		super("QR code not found with id: " + qrCodeId);
	}
}

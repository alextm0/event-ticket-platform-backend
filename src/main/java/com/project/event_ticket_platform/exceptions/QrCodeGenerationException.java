package com.project.event_ticket_platform.exceptions;

public class QrCodeGenerationException extends RuntimeException {

	public QrCodeGenerationException(String message) {
		super(message);
	}

	public QrCodeGenerationException(String message, Throwable cause) {
		super(message, cause);
	}
}


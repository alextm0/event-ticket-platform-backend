package com.project.event_ticket_platform.dtos;

import com.project.event_ticket_platform.entities.QrCodeStatusEnum;
import io.swagger.v3.oas.annotations.media.Schema;

import java.util.UUID;

@Schema(description = "QR code information")
public record QrCodeResponse(
	@Schema(description = "QR code ID", example = "123e4567-e89b-12d3-a456-426614174000")
	UUID id,

	@Schema(description = "QR code data (base64 encoded image or URL)")
	String qrCodeData,

	@Schema(description = "QR code status")
	QrCodeStatusEnum status
) {
}


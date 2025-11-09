package com.project.event_ticket_platform.dtos;

import com.project.event_ticket_platform.entities.QrCodeStatusEnum;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.Instant;
import java.util.UUID;

@Schema(description = "QR Code information")
public record QrCodeResponse(
	@Schema(description = "QR Code ID", example = "123e4567-e89b-12d3-a456-426614174000")
	UUID id,

	@Schema(description = "Base64 encoded QR code image")
	String codeData,

	@Schema(description = "QR Code status")
	QrCodeStatusEnum status,

	@Schema(description = "Generated at")
	Instant generatedAt
) {
}


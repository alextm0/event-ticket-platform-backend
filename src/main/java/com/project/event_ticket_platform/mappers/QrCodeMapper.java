package com.project.event_ticket_platform.mappers;

import com.project.event_ticket_platform.dtos.QrCodeResponse;
import com.project.event_ticket_platform.entities.QrCode;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;

@Mapper(
	componentModel = "spring",
	unmappedTargetPolicy = ReportingPolicy.IGNORE
)
public interface QrCodeMapper {

	@Mapping(source = "generatedDateTime", target = "generatedAt")
	QrCodeResponse toResponse(QrCode qrCode);
}


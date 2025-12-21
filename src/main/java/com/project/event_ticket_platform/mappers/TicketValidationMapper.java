package com.project.event_ticket_platform.mappers;

import com.project.event_ticket_platform.dtos.TicketValidationResponse;
import com.project.event_ticket_platform.entities.TicketValidation;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface TicketValidationMapper {

	@Mapping(source = "event.id", target = "eventId")
	@Mapping(source = "event.title", target = "eventTitle")
	@Mapping(source = "ticket.id", target = "ticketId")
	@Mapping(source = "qrCodeData", target = "qrCodeId")
	@Mapping(source = "ticket.ticketType.event.id", target = "ticketEventId")
	@Mapping(source = "ticket.ticketType.event.title", target = "ticketEventTitle")
	@Mapping(source = "ticket.status", target = "ticketStatus")
	@Mapping(source = "status", target = "validationStatus")
	@Mapping(source = "validationDateTime", target = "validatedAt")
	TicketValidationResponse toResponse(TicketValidation validation);
}

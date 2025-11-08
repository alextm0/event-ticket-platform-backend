package com.project.event_ticket_platform.mappers;

import com.project.event_ticket_platform.dtos.TicketResponse;
import com.project.event_ticket_platform.entities.Ticket;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;

@Mapper(
	componentModel = "spring",
	unmappedTargetPolicy = ReportingPolicy.IGNORE
)
public interface TicketMapper {

	@Mapping(source = "qrCode.id", target = "qrCodeId")
	@Mapping(source = "ticketType.event.title", target = "eventTitle")
	@Mapping(source = "ticketType.event.location", target = "eventLocation")
	@Mapping(source = "ticketType.event.startTime", target = "eventStartTime")
	@Mapping(source = "ticketType.name", target = "ticketTypeName")
	TicketResponse toResponse(Ticket ticket);
}


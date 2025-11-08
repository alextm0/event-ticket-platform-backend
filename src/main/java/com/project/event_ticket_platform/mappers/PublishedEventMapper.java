package com.project.event_ticket_platform.mappers;

import com.project.event_ticket_platform.dtos.PublishedEventResponse;
import com.project.event_ticket_platform.entities.Event;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;

@Mapper(
	componentModel = "spring",
	uses = {TicketTypeMapper.class},
	unmappedTargetPolicy = ReportingPolicy.IGNORE
)
public interface PublishedEventMapper {

	@Mapping(source = "ticketTypes", target = "ticketTypes")
	PublishedEventResponse toResponse(Event event);
}


package com.project.event_ticket_platform.mappers;

import com.project.event_ticket_platform.dtos.TicketTypeResponse;
import com.project.event_ticket_platform.entities.TicketType;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;

import java.util.List;

@Mapper(
	componentModel = "spring",
	unmappedTargetPolicy = ReportingPolicy.IGNORE
)
public interface TicketTypeMapper {

	@Mapping(source = "active", target = "isActive")
	@Mapping(expression = "java(ticketType.getTotalQuantity() - ticketType.getSoldCount())", target = "availableQuantity")
	TicketTypeResponse toResponse(TicketType ticketType);

	List<TicketTypeResponse> toResponseList(List<TicketType> ticketTypes);
}


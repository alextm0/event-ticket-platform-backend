package com.project.event_ticket_platform.mappers;

import com.project.event_ticket_platform.dtos.TicketTypeResponse;
import com.project.event_ticket_platform.entities.TicketType;
import org.springframework.stereotype.Component;

@Component
public class TicketTypeMapper {

	public TicketTypeResponse toResponse(TicketType ticketType) {
		int availableQuantity = ticketType.getTotalQuantity() - ticketType.getSoldCount();

		return new TicketTypeResponse(
			ticketType.getId(),
			ticketType.getName(),
			ticketType.getDescription(),
			ticketType.getPrice(),
			ticketType.getTotalQuantity(),
			ticketType.getSoldCount(),
			availableQuantity,
			ticketType.isActive()
		);
	}
}


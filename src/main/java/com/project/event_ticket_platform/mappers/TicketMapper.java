package com.project.event_ticket_platform.mappers;

import com.project.event_ticket_platform.dtos.TicketResponse;
import com.project.event_ticket_platform.entities.Ticket;
import org.springframework.stereotype.Component;

@Component
public class TicketMapper {

	public TicketResponse toResponse(Ticket ticket) {
		return new TicketResponse(
			ticket.getId(),
			ticket.getStatus(),
			ticket.getTicketType().getEvent().getTitle(),
			ticket.getTicketType().getEvent().getLocation(),
			ticket.getTicketType().getEvent().getStartTime(),
			ticket.getTicketType().getName(),
			ticket.getQrCode().getId(),
			ticket.getCheckedInAt(),
			ticket.getCreatedAt()
		);
	}
}


package com.project.event_ticket_platform.mappers;

import com.project.event_ticket_platform.dtos.PublishedEventResponse;
import com.project.event_ticket_platform.entities.Event;
import org.springframework.stereotype.Component;

import java.util.stream.Collectors;

@Component
public class PublishedEventMapper {

	private final TicketTypeMapper ticketTypeMapper;

	public PublishedEventMapper(TicketTypeMapper ticketTypeMapper) {
		this.ticketTypeMapper = ticketTypeMapper;
	}

	public PublishedEventResponse toResponse(Event event) {
		return new PublishedEventResponse(
			event.getId(),
			event.getTitle(),
			event.getDescription(),
			event.getLocation(),
			event.getStartTime(),
			event.getEndTime(),
			event.getStatus(),
			event.getOrganizer().getName(),
			event.getTicketTypes().stream()
				.map(ticketTypeMapper::toResponse)
				.collect(Collectors.toList())
		);
	}
}


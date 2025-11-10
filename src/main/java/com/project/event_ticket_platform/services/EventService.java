package com.project.event_ticket_platform.services;

import com.project.event_ticket_platform.dtos.CreateEventRequest;
import com.project.event_ticket_platform.dtos.EventResponse;
import com.project.event_ticket_platform.dtos.EventTicketSaleResponse;
import com.project.event_ticket_platform.dtos.UpdateEventRequest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.UUID;

public interface EventService {

	EventResponse createEvent(CreateEventRequest request);

	Page<EventResponse> getAllEvents(Pageable pageable);

	EventResponse updateEvent(UUID eventId, UpdateEventRequest request);

	void deleteEvent(UUID eventId);

	Page<EventTicketSaleResponse> getTicketSalesForEvent(UUID eventId, Pageable pageable);

    EventTicketSaleResponse getTicketSaleForEvent(UUID eventId, UUID ticketId);
}

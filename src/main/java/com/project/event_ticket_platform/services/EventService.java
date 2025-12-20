package com.project.event_ticket_platform.services;

import com.project.event_ticket_platform.dtos.*;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.UUID;

public interface EventService {

	EventResponse createEvent(CreateEventRequest request);

	EventResponse getEventById(UUID eventId);

	Page<EventResponse> getAllEvents(Pageable pageable);

	Page<EventResponse> getEventsByOrganizer(UUID organizerId, Pageable pageable);

	EventResponse updateEvent(UUID eventId, UpdateEventRequest request);

	void deleteEvent(UUID eventId);

	Page<EventTicketSaleResponse> getTicketSalesForEvent(UUID eventId, Pageable pageable);

	EventTicketSaleResponse getTicketSaleForEvent(UUID eventId, UUID ticketId);

	Page<TicketTypeResponse> getTicketTypesForEvent(UUID eventId, Pageable pageable);

	TicketTypeResponse getTicketTypeForEvent(UUID eventId, UUID ticketTypeId);

	TicketTypeResponse createTicketTypeForEvent(UUID eventId, CreateTicketTypeRequest request);

	void deleteTicketTypeForEvent(UUID eventId, UUID ticketTypeId);

	TicketTypeResponse patchTicketTypeForEvent(UUID eventId, UUID ticketTypeId, PatchTicketTypeRequest request);

	StaffAssignedEventsResponse getAssignedEventsForStaff(UUID staffId);

	void assignStaffToEvent(UUID eventId, UUID staffId);

	void removeStaffFromEvent(UUID eventId, UUID staffId);

	List<UserResponse> getEventStaff(UUID eventId);
}
